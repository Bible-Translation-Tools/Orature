package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import com.sun.glass.ui.Screen
import io.reactivex.Completable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.markerapp.app.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import javafx.application.Application

const val SECONDS_ON_SCREEN = 10
private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#F7FAFF"

class ScopeVM: ViewModel() {
    val parametersProperty = SimpleObjectProperty<Application.Parameters>()

    val parameters
        get() = parametersProperty.value
}

class VerseMarkerViewModel : ViewModel() {

    val scopeVM: ScopeVM by inject()
    val logger = LoggerFactory.getLogger(VerseMarkerViewModel::class.java)

    val markers: VerseMarkerModel
    val audioPlayer = AudioBufferPlayer()
    var audioController: AudioPlayerController? = null
    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerRatioProperty = SimpleStringProperty()
    val headerTitle = SimpleStringProperty()
    val headerSubtitle = SimpleStringProperty()
    val positionProperty = SimpleDoubleProperty(0.0)
    val waveformImageProperty = SimpleObjectProperty<Image>()

    val width = Screen.getMainScreen().platformWidth
    val height = Screen.getMainScreen().platformHeight
    val padding = width / 2
    val imageWidth: Double

    init {
        val audioFile = File(scopeVM.parameters.named["wav"])
        val wav = WavFile(audioFile)
        val initialMarkerCount = wav.metadata.getCues().size
        val totalMarkers: Int =
            scopeVM.parameters.named["marker_total"]?.toInt() ?: initialMarkerCount
        headerTitle.set(scopeVM.parameters.named["action_title"])
        headerSubtitle.set(scopeVM.parameters.named["content_title"])
        markers = VerseMarkerModel(wav, totalMarkers)
        markers.markerCountProperty.onChangeAndDoNow {
            markerRatioProperty.set("$it/$totalMarkers")
        }
        audioPlayer.load(audioFile)
        imageWidth = computeImageWidth(SECONDS_ON_SCREEN)

        WaveformImageBuilder(
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        ).build(
            audioPlayer.getAudioReader()!!,
            fitToAudioMax = false,
            width = imageWidth.toInt(),
            height = height
        ).subscribe { image ->
            waveformImageProperty.set(image)
            audioPlayer.getAudioReader()?.seek(0)
        }
    }

    fun computeImageWidth(secondsOnScreen: Int): Double {
        val samplesPerScreenWidth = audioPlayer.getAudioReader()!!.sampleRate * secondsOnScreen
        val samplesPerPixel = samplesPerScreenWidth / width.toDouble()
        val pixelsInDuration = audioPlayer.getAbsoluteDurationInFrames() / samplesPerPixel
        return pixelsInDuration
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
        seek(markers.seekNext(audioPlayer.getAbsoluteLocationInFrames()))
    }

    fun seekPrevious() {
        seek(markers.seekPrevious(audioPlayer.getAbsoluteLocationInFrames()))
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
        markers.addMarker(audioPlayer.getAbsoluteLocationInFrames())
    }

    fun calculatePosition() {
        val current = audioPlayer.getAbsoluteLocationInFrames()
        val duration = audioPlayer.getAbsoluteDurationInFrames().toDouble()
        val percentPlayed = current / duration
        val pos = percentPlayed * imageWidth
        positionProperty.set(pos)
    }

    fun getLocationInFrames(): Int {
        return audioPlayer.getAbsoluteLocationInFrames()
    }

    fun getDurationInFrames(): Int {
        return audioPlayer.getAbsoluteDurationInFrames()
    }
}
