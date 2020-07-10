package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.common.io.wav.WavFile
import org.wycliffeassociates.otter.common.io.wav.WavFileReader
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioPlayer
import org.wycliffeassociates.otter.jvm.markerapp.app.model.VerseMarkers
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File

class VerseMarkerViewModel: ViewModel() {

    val audioPlayer = AudioBufferPlayer()
    val markers: VerseMarkers
    var audioController: AudioPlayerController? = null
    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerCountProperty = SimpleIntegerProperty(0)

    init {
        val scope = scope as ParameterizedScope
        val audioFile = File(scope.parameters.named["wav"])
        val wav = WavFile(audioFile)
        markers = VerseMarkers(wav)
        markerCountProperty.bind(markers.markerCountProperty)
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
        audioPlayer.seek(markers.seekNext(audioPlayer.getAbsoluteLocationInFrames()))
    }

    fun seekPrevious() {
        audioPlayer.seek(markers.seekPrevious(audioPlayer.getAbsoluteLocationInFrames()))
    }
}