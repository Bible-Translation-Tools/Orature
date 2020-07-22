package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.common.io.wav.WavFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.markerapp.app.model.VerseMarkers
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File

class VerseMarkerViewModel: ViewModel() {

    val audioPlayer = AudioBufferPlayer()
    val markers: VerseMarkers
    var audioController: AudioPlayerController? = null
    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerCountProperty = SimpleStringProperty("0/0")

    init {
        val scope = scope as ParameterizedScope
        val audioFile = File(scope.parameters.named["wav"])
        val totalMarkers: Int = scope.parameters.named["marker_total"]?.toInt() ?: 10
        val wav = WavFile(audioFile)
        markers = VerseMarkers(wav, totalMarkers)
        markers.markerCountProperty.onChangeAndDoNow {
            markerCountProperty.set("${it}/$totalMarkers")
        }
        audioPlayer.load(audioFile)
    }

    fun initializeAudioController(slider: Slider) {
        audioController = AudioPlayerController(audioPlayer, slider)
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun mediaToggle() {
        audioController?.toggle()
    }

    fun seekNext() {
        audioController?.seekLocation(markers.seekNext(audioPlayer.getAbsoluteLocationInFrames()))
    }

    fun seekPrevious() {
        audioController?.seekLocation(markers.seekPrevious(audioPlayer.getAbsoluteLocationInFrames()))
    }
}