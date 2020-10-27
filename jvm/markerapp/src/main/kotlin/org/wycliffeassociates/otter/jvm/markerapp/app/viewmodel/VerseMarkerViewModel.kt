package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import com.sun.glass.ui.Screen
import io.reactivex.Completable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.markerapp.app.model.VerseMarkers
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File

const val SECONDS_ON_SCREEN = 10;

class VerseMarkerViewModel : ViewModel() {

    val markers: VerseMarkers
    val audioPlayer = AudioBufferPlayer()
    var audioController: AudioPlayerController? = null
    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerRatioProperty = SimpleStringProperty()
    val headerTitle = SimpleStringProperty()
    val headerSubtitle = SimpleStringProperty()
    val positionProperty = SimpleDoubleProperty(0.0)

    val width = Screen.getMainScreen().platformWidth
    val height = Screen.getMainScreen().platformHeight
    val padding = width / 2
    val imageWidth: Double

    init {
        val scope = scope as ParameterizedScope
        val audioFile = File(scope.parameters.named["wav"])
        val wav = WavFile(audioFile)
        val initialMarkerCount = wav.metadata.getCues().size
        val totalMarkers: Int =
            scope.parameters.named["marker_total"]?.toInt() ?: initialMarkerCount
        headerTitle.set(scope.parameters.named["action_title"])
        headerSubtitle.set(scope.parameters.named["content_title"])
        markers = VerseMarkers(wav, totalMarkers)
        markers.markerCountProperty.onChangeAndDoNow {
            markerRatioProperty.set("${it}/$totalMarkers")
        }
        audioPlayer.load(audioFile)
        imageWidth = computeImageWidth(SECONDS_ON_SCREEN)
    }

    fun computeImageWidth(secondsOnScreen: Int): Double {
        val samplesPerScreenWidth = audioPlayer.getAudioReader()!!.sampleRate * secondsOnScreen
        val samplesPerPixel = samplesPerScreenWidth / width.toDouble()
        val pixelsInDuration =  audioPlayer.getAbsoluteDurationInFrames() / samplesPerPixel
        return pixelsInDuration
    }

    fun initializeAudioController(slider: Slider) {
        audioController = AudioPlayerController(slider)
        audioController?.load(audioPlayer)
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun mediaToggle() {
        audioController?.toggle()
    }

    fun seekNext() {
        audioController?.seek(markers.seekNext(audioPlayer.getAbsoluteLocationInFrames()))
    }

    fun seekPrevious() {
        audioController?.seek(markers.seekPrevious(audioPlayer.getAbsoluteLocationInFrames()))
    }

    fun writeMarkers(): Completable {
        if (isPlayingProperty.value) audioController?.toggle()
        audioPlayer.close()
        return markers.writeMarkers()
    }

    fun placeMarker() {
        markers.addMarker(audioPlayer.getAbsoluteLocationInFrames())
    }

    fun calculatePosition() {
        val current = audioPlayer.getAbsoluteLocationInFrames()
        val duration = audioPlayer.getAbsoluteDurationInFrames().toDouble()
        val percentPlayed =  current / duration
        val pos = percentPlayed * imageWidth
        positionProperty.set(pos)
    }
}
