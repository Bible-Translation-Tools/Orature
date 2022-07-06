/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.*
import javafx.beans.value.ChangeListener
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import java.lang.Integer.min
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import kotlin.math.max
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder

private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#FFFFFF"

class VerseMarkerViewModel : ViewModel(), IMarkerViewModel {

    private val logger = LoggerFactory.getLogger(VerseMarkerViewModel::class.java)

    private val width = Screen.getMainScreen().platformWidth
    private val height = min(Screen.getMainScreen().platformHeight, 500)

    val waveformMinimapImage = SimpleObjectProperty<Image>()

    /** Call this before leaving the view to avoid memory leak */
    var imageCleanup: () -> Unit = {}

    private val asyncBuilder = ObservableWaveformBuilder()
    lateinit var waveform: Observable<Image>

    lateinit var waveformMinimapImageListener: ChangeListener<Image>

    override val currentMarkerNumberProperty = SimpleIntegerProperty(0)

    override var markerModel: VerseMarkerModel? = null
    override val markers = observableListOf<ChunkMarkerModel>()
    override val markerCountProperty = markers.sizeProperty

    override var audioController: AudioPlayerController? = null

    override val audioPlayer = SimpleObjectProperty<IAudioPlayer>()

    val isLoadingProperty = SimpleBooleanProperty(false)
    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerRatioProperty = SimpleStringProperty()
    val headerTitle = SimpleStringProperty()
    val headerSubtitle = SimpleStringProperty()
    val compositeDisposable = CompositeDisposable()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty()

    private var sampleRate: Int = 0 // beware of divided by 0
    private var totalFrames: Int = 0 // beware of divided by 0
    override var resumeAfterScroll = false

    fun onDock(op: () -> Unit) {
        isLoadingProperty.set(true)
        val audio = loadAudio()
        loadMarkers(audio)
        loadTitles()
        createWaveformImages(audio)
        op.invoke()
    }

    private fun loadAudio(): AudioFile {
        val scope = scope as ParameterizedScope
        val player = (scope.workspace.params["audioConnectionFactory"] as AudioConnectionFactory).getPlayer()
        val audioFile = File(scope.parameters.named["wav"])
        val audio = AudioFile(audioFile)
        player.load(audioFile)
        player.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
        }
        audioPlayer.set(player)
        return audio
    }

    private fun loadMarkers(audio: AudioFile) {
        val initialMarkerCount = audio.metadata.getCues().size
        scope as ParameterizedScope
        val totalMarkers: Int = scope.parameters.named["marker_total"]?.toInt() ?: initialMarkerCount
        markerModel = VerseMarkerModel(audio, totalMarkers)
        markerCountProperty.onChangeAndDoNow {
            markerRatioProperty.set("$it/$totalMarkers")
        }
        markerModel?.let { markerModel ->
            markers.setAll(markerModel.markers)
        }
    }

    private fun loadTitles() {
        scope as ParameterizedScope
        headerTitle.set(scope.parameters.named["action_title"])
        headerSubtitle.set(scope.parameters.named["content_title"])
    }

    private fun writeMarkers(): Completable {
        audioPlayer.get()?.pause()
        audioPlayer.get()?.close()
        return markerModel?.writeMarkers() ?: Completable.complete()
    }

    fun saveAndQuit() {
        logger.info("Closing Verse Marker app...")

        compositeDisposable.clear()
        waveformMinimapImage.set(null)
        currentMarkerNumberProperty.set(-1)
        imageCleanup()
        asyncBuilder.cancel()

        (scope as ParameterizedScope).let {
            writeMarkers()
                .doOnError { e ->
                    logger.error("Error in closing the maker app", e)
                }
                .subscribe {
                    runLater {
                        it.navigateBack()
                        System.gc()
                    }
                }
        }
    }

    fun initializeAudioController(slider: Slider) {
        audioController = AudioPlayerController(slider)
        audioController?.load(audioPlayer.get())
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun pause() {
        audioController?.pause()
    }

    private fun createWaveformImages(audio: AudioFile) {
        imageWidthProperty.set(computeImageWidth(SECONDS_ON_SCREEN))

        waveform = asyncBuilder.buildAsync(
            audio.reader(),
            width = imageWidthProperty.value.toInt(),
            height = Screen.getMainScreen().platformHeight,
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        )

        asyncBuilder
            .build(
                audio.reader(),
                width = Screen.getMainScreen().platformWidth,
                height = 50,
                wavColor = Color.web(WAV_COLOR),
                background = Color.web(BACKGROUND_COLOR)
            )
            .observeOnFx()
            .map { image ->
                waveformMinimapImage.set(image)
            }
            .ignoreElement()
            .andThen(waveform)
            .subscribe {
                runLater {
                    isLoadingProperty.set(false)
                }
            }
    }

    private fun computeImageWidth(secondsOnScreen: Int): Double {
        if (sampleRate == 0) {
            return 0.0
        }

        val samplesPerScreenWidth = sampleRate * secondsOnScreen
        val samplesPerPixel = samplesPerScreenWidth / width
        val pixelsInDuration = audioPlayer.get().getDurationInFrames() / samplesPerPixel
        return pixelsInDuration.toDouble()
    }

    fun pixelsInHighlight(controlWidth: Double): Double {
        if (sampleRate == 0 || totalFrames == 0) {
            return 1.0
        }

        val framesInHighlight = sampleRate * SECONDS_ON_SCREEN
        val framesPerPixel = totalFrames / max(controlWidth, 1.0)
        return max(framesInHighlight / framesPerPixel, 1.0)
    }
}
