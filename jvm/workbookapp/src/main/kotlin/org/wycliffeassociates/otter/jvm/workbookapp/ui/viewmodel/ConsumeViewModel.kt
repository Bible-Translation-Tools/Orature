/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.getWaveformColors
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.common.domain.model.MarkerItem
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementModel
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementType
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.controls.waveform.WAVEFORM_MAX_HEIGHT
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.io.File
import javax.inject.Inject

class ConsumeViewModel : ViewModel(), IMarkerViewModel {

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()
    val settingsViewModel: SettingsViewModel by inject()

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    lateinit var audio: OratureAudioFile

    lateinit var waveform: Observable<Image>

    var subscribeOnWaveformImagesProperty = SimpleObjectProperty {}
    val cleanupWaveformProperty = SimpleObjectProperty {}

    override var markerModel: MarkerPlacementModel? = null
    override val markers = observableListOf<MarkerItem>()
    override val markerCountProperty = markers.sizeProperty
    override val highlightedMarkerIndexProperty = SimpleIntegerProperty(-1)
    override var resumeAfterScroll: Boolean = false

    override var audioController: AudioPlayerController? = null
    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    override var sampleRate: Int = 0 // beware of divided by 0
    override val totalFramesProperty = SimpleIntegerProperty(0)
    override var totalFrames: Int by totalFramesProperty // beware of divided by 0
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty(0.0)

    override val audioPositionProperty = SimpleIntegerProperty()
    val compositeDisposable = CompositeDisposable()

    val isPlayingProperty = SimpleBooleanProperty(false)

    private val builder = ObservableWaveformBuilder()
    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, WAVEFORM_MAX_HEIGHT.toInt())

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDockConsume() {
        val wb = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter
        Maybe
            .fromCallable<SourceAudio> {
                wb.sourceAudioAccessor.getChapter(chapter.sort, wb.target)
                    ?: wb.sourceAudioAccessor.getUserMarkedChapter(chapter.sort, wb.target)
            }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doFinally {
                translationViewModel.loadingStepProperty.set(false)
            }
            .subscribe { sa ->
                audioDataStore.sourceAudioProperty.set(sa)
                audio = loadAudio(sa.file)
                createWaveformImages(audio)
                subscribeOnWaveformImages()
                loadSourceMarkers(audio)
            }

        translationViewModel.currentMarkerProperty.bind(highlightedMarkerIndexProperty)
    }

    fun onUndockConsume() {
        pause()
        cleanup()
        translationViewModel.currentMarkerProperty.unbind()
        translationViewModel.currentMarkerProperty.set(-1)
    }

    fun onThemeChange() {

        // Avoids null error in createWaveformImages cause by player not yet being initialized.
        val hasAudioAndPlayer =
            waveformAudioPlayerProperty.value != null && waveformAudioPlayerProperty.value.getDurationInFrames() > 0

        if (!hasAudioAndPlayer) {
            return
        }

        val audioFile = audioDataStore.sourceAudioProperty.value?.file
        audioFile?.let {
            pause()
            builder.cancel()
            cleanupWaveform()
            createWaveformImages(OratureAudioFile(audioFile))
            subscribeOnWaveformImages()
        }
    }

    fun pause() {
        audioController?.pause()
    }

    private fun loadSourceMarkers(audio: OratureAudioFile) {
        audio.clearCues()
        val verseMarkers = audio.getMarker<VerseMarker>()
        markerModel = MarkerPlacementModel(
            MarkerPlacementType.VERSE,
            audio,
            verseMarkers.map { it }
        )
        markerModel?.let { markerModel ->
            markers.setAll(markerModel.markerItems)
        }
    }


    private fun createWaveformImages(audio: OratureAudioFile) {
        imageWidthProperty.set(computeImageWidth(width, SECONDS_ON_SCREEN))

        val waveformColors = getWaveformColors(settingsViewModel.appColorMode.value)

        waveformColors?.let {
            builder.cancel()
            waveform = builder.buildAsync(
                audio.reader(),
                width = imageWidthProperty.value.toInt(),
                height = height,
                wavColor = Color.web(waveformColors.wavColorHex),
                background = Color.web(waveformColors.backgroundColorHex)
            )
        }
    }

    fun cleanup() {
        audioConnectionFactory.releasePlayer()
        builder.cancel()
        compositeDisposable.clear()
        markerModel = null
        cleanupWaveform()
    }

    fun cleanupWaveform() {
        cleanupWaveformProperty.value.invoke()
    }

    fun subscribeOnWaveformImages() {
        subscribeOnWaveformImagesProperty.value.invoke()
    }

    private fun loadAudio(audioFile: File): OratureAudioFile {
        val player = audioConnectionFactory.getPlayer()
        val audio = OratureAudioFile(audioFile)
        player.load(audioFile)
        player.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
            totalFramesProperty.set(totalFrames)
        }

        waveformAudioPlayerProperty.set(player)
        audioController = AudioPlayerController().also {
            it.load(player)
            isPlayingProperty.bind(it.isPlayingProperty)
        }

        return audio
    }
}