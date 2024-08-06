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
package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.*
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementModel
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import java.lang.Integer.min
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.beans.binding.Bindings
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.getWaveformColors
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseFinishedEvent
import org.wycliffeassociates.otter.common.domain.model.MarkerItem
import org.wycliffeassociates.otter.common.domain.model.MarkerPlacementType
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.controls.waveform.WAVEFORM_MAX_HEIGHT
import java.util.regex.Pattern

class VerseMarkerViewModel : ViewModel(), IMarkerViewModel {

    private val logger = LoggerFactory.getLogger(VerseMarkerViewModel::class.java)

    private val width = Screen.getMainScreen().platformWidth
    private val height = min(Screen.getMainScreen().platformHeight, WAVEFORM_MAX_HEIGHT.toInt())

    val themeColorProperty = SimpleObjectProperty<ColorTheme>()

    private val asyncBuilder = ObservableWaveformBuilder()
    lateinit var waveform: Observable<Image>
    val waveformMinimapImage = SimpleObjectProperty<Image>()

    override val highlightedMarkerIndexProperty = SimpleIntegerProperty(0)
    override val audioPositionProperty = SimpleIntegerProperty()
    override var markerModel: MarkerPlacementModel? = null
    override val markers = observableListOf<MarkerItem>()
    override val markerCountProperty = markers.sizeProperty
    override var sampleRate: Int = 0 // beware of divided by 0
    override val totalFramesProperty = SimpleIntegerProperty(0)
    override var totalFrames: Int by totalFramesProperty // beware of divided by 0
    override var audioController: AudioPlayerController? = null

    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()

    val isLoadingProperty = SimpleBooleanProperty(false)
    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerRatioProperty = SimpleStringProperty()
    val headerTitle = SimpleStringProperty()
    val headerSubtitle = SimpleStringProperty()
    val compositeDisposable = CompositeDisposable()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty()
    private val onThemeChangeAction = SimpleObjectProperty<() -> Unit>()

    override var resumeAfterScroll = false

    var cleanupWaveform: () -> Unit = {}

    private var timer: AnimationTimer? = object : AnimationTimer() {
        override fun handle(currentNanoTime: Long) {
            calculatePosition()
        }
    }

    fun onDock(op: () -> Unit) {
        onThemeChangeAction.set(op)
        themeColorProperty.set(getThemeFromRoot())
        timer?.start()
        isLoadingProperty.set(true)
        val audio = loadAudio()
        loadMarkers(audio)
        loadTitles()
        createWaveformImages(audio)
        op.invoke()

        themeColorProperty.bind(
            Bindings.createObjectBinding(
                {
                    getThemeFromRoot()
                },
                primaryStage.scene.root.styleClass
            )
        )
    }

    private fun getThemeFromRoot(): ColorTheme {
        return if (primaryStage.scene.root.styleClass.contains(ColorTheme.DARK.styleClass)) {
            ColorTheme.DARK
        } else {
            ColorTheme.LIGHT
        }
    }

    fun onThemeChange() {
        val audioFile = loadAudio()
        audioFile.let {
            pause()
            asyncBuilder.cancel()
            cleanupWaveform()
            createWaveformImages(OratureAudioFile(it.file))
            onThemeChangeAction.value.invoke()
        }
    }

    private fun loadAudio(): OratureAudioFile {
        val scope = scope as ParameterizedScope
        val player = (scope.workspace.params["audioConnectionFactory"] as AudioConnectionFactory).getPlayer()
        val audioFile = File(scope.parameters.named["wav"])
        val audio = OratureAudioFile(audioFile)
        player.load(audioFile)
        player.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
        }
        waveformAudioPlayerProperty.set(player)
        return audio
    }

    private fun loadMarkers(audio: OratureAudioFile) {
        val params = (scope as ParameterizedScope).parameters
        val audioMarkers = loadMarkersFromParameters(params)
        val totalMarkers: Int = audioMarkers.size

        markerModel = MarkerPlacementModel(MarkerPlacementType.VERSE, audio, audioMarkers)

        markerRatioProperty.bind(
            Bindings.createStringBinding(
                { "${markerCountProperty.value}/${markerModel?.markerTotal}" },
                markerCountProperty
            )
        )
        markerModel?.let { markerModel ->
            markers.setAll(markerModel.markerItems)
        }
    }

    private fun loadMarkersFromParameters(params: Application.Parameters): List<AudioMarker> {
        val labels = getVerseLabelList(params.named["marker_labels"])
        val verses = verseLabelsToMarkers(labels)
        val titles = getTitleMarkers(params.named["book_slug"], params.named["chapter_number"])
        return listOf(*titles, *verses)
    }

    private fun getTitleMarkers(bookSlug: String?, chapterNumber: String?): Array<AudioMarker> {
        if (bookSlug == null || chapterNumber == null) return arrayOf()

        val chapterNumber = Integer.parseInt(chapterNumber)
        return when (chapterNumber) {
            1 -> arrayOf(BookMarker(bookSlug, 0), ChapterMarker(chapterNumber, 0))
            else -> arrayOf(ChapterMarker(chapterNumber, 0))
        }
    }

    private fun getVerseLabelList(s: String?): List<String> {
        return s
            ?.removeSurrounding("[", "]")
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()
    }

    private fun parseLabel(label: String): Pair<Int, Int> {
        val pattern = Pattern.compile("(\\d+)(?:-(\\d+))?")
        val match = pattern.matcher(label)
        if (match.matches()) {
            val start: Int = match.group(1)!!.toInt()
            val end: Int = match.group(2)?.toInt() ?: start
            return Pair(start, end)
        } else {
            throw NumberFormatException(
                "Invalid verse label: $label, which could not be parsed to a verse or verse range"
            )
        }
    }

    private fun verseLabelsToMarkers(list: List<String>): Array<VerseMarker> {
        return list.map {
            val (start, end) = parseLabel(it)
            VerseMarker(start, end, 0)
        }.toTypedArray()
    }

    private fun loadTitles() {
        val paramScope = scope as ParameterizedScope
        headerTitle.set(paramScope.parameters.named["action_title"])
        headerSubtitle.set(paramScope.parameters.named["content_title"])
    }

    private fun writeMarkers(): Completable {
        waveformAudioPlayerProperty.get()?.pause()
        waveformAudioPlayerProperty.get()?.close()
        return markerModel?.writeMarkers() ?: Completable.complete()
    }

    fun saveAndQuit() {
        logger.info("Saving Marker data...")
        timer?.stop()
        compositeDisposable.clear()
        waveformMinimapImage.set(null)
        highlightedMarkerIndexProperty.set(-1)
        audioController!!.release()
        audioController = null
        asyncBuilder.cancel()
        cleanupWaveform()

        writeMarkers()
            .doOnError { e ->
                logger.error("Error in closing the maker app", e)
            }
            .subscribe {
                runLater {
                    logger.info("Close Marker")
                    fire(PluginCloseFinishedEvent)
                    (scope as ParameterizedScope).navigateBack()
                    System.gc()
                }
            }
    }

    fun initializeAudioController(slider: Slider) {
        audioController = AudioPlayerController(slider)
        audioController?.load(waveformAudioPlayerProperty.get())
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun pause() {
        audioController?.pause()
    }

    private fun createWaveformImages(audio: OratureAudioFile) {
        imageWidthProperty.set(computeImageWidth(SECONDS_ON_SCREEN))

        val waveformColors = getWaveformColors(themeColorProperty.value)

        waveform = asyncBuilder.buildAsync(
            audio.reader(),
            width = imageWidthProperty.value.toInt(),
            height = Screen.getMainScreen().platformHeight,
            wavColor = Color.web(waveformColors.wavColorHex),
            background = Color.web(waveformColors.backgroundColorHex)
        )

        asyncBuilder
            .build(
                audio.reader(),
                width = Screen.getMainScreen().platformWidth,
                height = 50,
                wavColor = Color.web(waveformColors.wavColorHex),
                background = Color.web(waveformColors.backgroundColorHex)
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
        val pixelsInDuration = waveformAudioPlayerProperty.get().getDurationInFrames() / samplesPerPixel
        return pixelsInDuration.toDouble()
    }
}
