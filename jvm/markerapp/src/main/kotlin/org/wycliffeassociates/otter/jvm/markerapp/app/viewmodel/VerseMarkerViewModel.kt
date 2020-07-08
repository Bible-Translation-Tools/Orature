package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.device.audio.AudioPlayer
import tornadofx.*

class VerseMarkerViewModel: ViewModel() {

    val audioPlayer = AudioPlayer()
    var audioController: AudioPlayerController? = null
    val isPlayingProperty = SimpleBooleanProperty(false)

    fun initializeAudioController(slider: Slider) {
        audioController = AudioPlayerController(audioPlayer, slider)
        isPlayingProperty.bind(audioController!!.isPlayingProperty)
    }

    fun mediaToggle() {
        audioController?.toggle()
    }
    fun seek() {

    }
}