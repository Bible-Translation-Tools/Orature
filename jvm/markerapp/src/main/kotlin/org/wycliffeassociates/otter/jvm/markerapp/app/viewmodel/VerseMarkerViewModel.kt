/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.markerapp.app.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import java.lang.Integer.min

const val SECONDS_ON_SCREEN = 10
private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#F7FAFF"

class VerseMarkerViewModel : ViewModel() {

    private val width = Screen.getMainScreen().platformWidth
    private val height = min(Screen.getMainScreen().platformHeight, 500)

    val logger = LoggerFactory.getLogger(VerseMarkerViewModel::class.java)

    val markers: VerseMarkerModel
    val audioPlayer = (scope.workspace.params["audioConnectionFactory"] as AudioConnectionFactory).getPlayer()
    var audioController: AudioPlayerController? = null
    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerRatioProperty = SimpleStringProperty()
    val headerTitle = SimpleStringProperty()
    val headerSubtitle = SimpleStringProperty()
    val positionProperty = SimpleDoubleProperty(0.0)
    val waveformMinimapImage = SimpleObjectProperty<Image>()
    val waveform: Observable<Image>
    val imageWidth: Double

    private val audioFile: File

    init {
        val scope = scope as ParameterizedScope
        audioFile = File(scope.parameters.named["wav"])
        val wav = AudioFile(audioFile)
        val initialMarkerCount = wav.metadata.getCues().size
        val totalMarkers: Int =
            scope.parameters.named["marker_total"]?.toInt() ?: initialMarkerCount
        headerTitle.set(scope.parameters.named["action_title"])
        headerSubtitle.set(scope.parameters.named["content_title"])
        markers = VerseMarkerModel(wav, totalMarkers)
        markers.markerCountProperty.onChangeAndDoNow {
            markerRatioProperty.set("$it/$totalMarkers")
        }
        audioPlayer.load(audioFile)
        imageWidth = computeImageWidth(SECONDS_ON_SCREEN)

        WaveformImageBuilder(
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        ).apply {
            build(
                AudioFile(audioFile).reader(),
                width = imageWidth.toInt(),
                height = 50
            )
                .observeOnFx()
                .subscribe { image ->
                    waveformMinimapImage.set(image)
                }

            waveform = buildWaveformAsync(
                AudioFile(audioFile).reader(),
                width = imageWidth.toInt(),
                height = height
            )
        }
    }

    fun computeImageWidth(secondsOnScreen: Int): Double {
        val samplesPerScreenWidth = audioPlayer.getAudioReader()!!.sampleRate * secondsOnScreen
        val samplesPerPixel = samplesPerScreenWidth / width
        val pixelsInDuration = audioPlayer.getDurationInFrames() / samplesPerPixel
        return pixelsInDuration.toDouble()
    }

    fun initializeAudioController(slider: Slider) {
        audioController = AudioPlayerController(slider)
        audioController?.load(audioPlayer)
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun pause() {
        audioController?.pause()
    }

    fun mediaToggle() {
        audioController?.toggle()
    }

    fun seek(location: Int) {
        audioController?.seek(location)
    }

    fun seekNext() {
        val wasPlaying = audioPlayer.isPlaying()
        if (wasPlaying) {
            audioController?.toggle()
        }
        seek(markers.seekNext(audioPlayer.getLocationInFrames()))
        if (wasPlaying) {
            audioController?.toggle()
        }
    }

    fun seekPrevious() {
        val wasPlaying = audioPlayer.isPlaying()
        if (wasPlaying) {
            audioController?.toggle()
        }
        seek(markers.seekPrevious(audioPlayer.getLocationInFrames()))
        if (wasPlaying) {
            audioController?.toggle()
        }
    }

    fun writeMarkers(): Completable {
        if (isPlayingProperty.value) audioController?.toggle()
        audioPlayer.close()
        return markers.writeMarkers()
    }

    fun saveAndQuit() {
        (scope as ParameterizedScope).let {
            writeMarkers()
                .doOnError { e ->
                    logger.error("Error in closing the maker app", e)
                }
                .subscribe {
                    it.navigateBack()
                }
        }
    }

    fun placeMarker() {
        markers.addMarker(audioPlayer.getLocationInFrames())
    }

    fun calculatePosition() {
        val current = audioPlayer.getLocationInFrames()
        val duration = audioPlayer.getDurationInFrames().toDouble()
        val percentPlayed = current / duration
        val pos = percentPlayed * imageWidth
        positionProperty.set(pos)
    }

    fun getLocationInFrames(): Int {
        return audioPlayer.getLocationInFrames()
    }

    fun getDurationInFrames(): Int {
        return audioPlayer.getDurationInFrames()
    }
}
