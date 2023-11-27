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
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.common.domain.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import java.lang.Integer.min
import javafx.animation.AnimationTimer
import javafx.beans.binding.Bindings
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.PluginCloseFinishedEvent
import org.wycliffeassociates.otter.common.domain.model.ChunkMarkerModel
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
    override val audioPositionProperty = SimpleIntegerProperty()
    override var markerModel: VerseMarkerModel? = null
    override val markers = observableListOf<ChunkMarkerModel>()
    override val markerCountProperty = markers.sizeProperty
    override var sampleRate: Int = 0 // beware of divided by 0
    override var totalFrames: Int = 0 // beware of divided by 0
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

    override var resumeAfterScroll = false

    private var timer: AnimationTimer? = object : AnimationTimer() {
        override fun handle(currentNanoTime: Long) {
            calculatePosition()
        }
    }

    fun onDock(op: () -> Unit) {
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
        waveformAudioPlayerProperty.set(player)
        return audio
    }

    private fun loadMarkers(audio: OratureAudioFile) {
        val params = (scope as ParameterizedScope).parameters
        val markersList: List<String> = getVerseLabelList(params.named["marker_labels"])
        val totalMarkers: Int = markersList.size

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
        audioController?.load(waveformAudioPlayerProperty.get())
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
        val pixelsInDuration = waveformAudioPlayerProperty.get().getDurationInFrames() / samplesPerPixel
        return pixelsInDuration.toDouble()
    }
}
