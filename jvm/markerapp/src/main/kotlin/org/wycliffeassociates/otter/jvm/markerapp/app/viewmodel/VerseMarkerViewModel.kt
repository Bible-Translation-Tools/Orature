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
import javafx.beans.property.*
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.audio.decorators.OratureAudioFile
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import java.lang.Integer.min
import javafx.animation.AnimationTimer
import javafx.beans.binding.Bindings
import org.wycliffeassociates.otter.common.domain.audio.decorators.OratureCueType
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseFinishedEvent
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

    val themeColorProperty = SimpleObjectProperty(ColorTheme.LIGHT)

    private val asyncBuilder = ObservableWaveformBuilder()
    lateinit var waveform: Observable<Image>
    val waveformMinimapImage = SimpleObjectProperty<Image>()

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

    val timer = object : AnimationTimer() {
        override fun handle(currentNanoTime: Long) {
            calculatePosition()
        }
    }

    fun onDock(op: () -> Unit) {
        timer.start()
        isLoadingProperty.set(true)
        val audio = loadAudio()
        loadMarkers(audio)
        loadTitles()
        createWaveformImages(audio)
        op.invoke()

        themeColorProperty.bind(
            Bindings.createObjectBinding(
                {
                    if (primaryStage.scene.root.styleClass.contains(ColorTheme.DARK.styleClass)) {
                        ColorTheme.DARK
                    } else {
                        ColorTheme.LIGHT
                    }
                },
                primaryStage.scene.root.styleClass
            )
        )
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
        audioPlayer.set(player)
        return audio
    }

    private fun loadMarkers(audio: OratureAudioFile) {
        val initialMarkerCount = audio.getMarker(OratureCueType.VERSE).size
        scope as ParameterizedScope
        val markersList: List<String> = getVerseLabelList(scope.parameters.named["marker_labels"])
        val totalMarkers: Int = scope.parameters.named["marker_total"]?.toInt() ?: initialMarkerCount
        markerModel = VerseMarkerModel(audio, totalMarkers, markersList)

        markerRatioProperty.bind(
            Bindings.createStringBinding(
                { "${markerCountProperty.value}/$totalMarkers" },
                markerCountProperty
            )
        )
        markerModel?.let { markerModel ->
            markers.setAll(markerModel.markers)
        }
    }

    private fun getVerseLabelList(s: String?): List<String> {
        return s?.removeSurrounding("[", "]")?.split(",")?.map { it.trim() } ?: emptyList()
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
        logger.info("Saving Marker data...")
        timer.stop()
        compositeDisposable.clear()
        waveformMinimapImage.set(null)
        currentMarkerNumberProperty.set(-1)
        audioController!!.release()
        audioController = null
        asyncBuilder.cancel()

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
        audioController?.load(audioPlayer.get())
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun pause() {
        audioController?.pause()
    }

    private fun createWaveformImages(audio: OratureAudioFile) {
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
