package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
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

class VerseMarkerViewModel : ViewModel() {

    val markers: VerseMarkers
    val audioPlayer = AudioBufferPlayer()
    var audioController: AudioPlayerController? = null
    val isPlayingProperty = SimpleBooleanProperty(false)
    val markerRatioProperty = SimpleStringProperty()
    val headerTitle = SimpleStringProperty()
    val headerSubtitle = SimpleStringProperty()

    init {
        val scope = scope as ParameterizedScope
        val audioFile = File(scope.parameters.named["wav"])
        val wav = WavFile(audioFile)
        val initialMarkerCount = wav.metadata.getCues().size
        val totalMarkers: Int =
            scope.parameters.named["marker_total"]?.toInt() ?: initialMarkerCount
        markers = VerseMarkers(wav, totalMarkers)
        markers.markerCountProperty.onChangeAndDoNow {
            markerRatioProperty.set("${it}/$totalMarkers")
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
        audioController?.seek(markers.seekNext(audioPlayer.getAbsoluteLocationInFrames()))
    }

    fun seekPrevious() {
        audioController?.seek(markers.seekPrevious(audioPlayer.getAbsoluteLocationInFrames()))
    }
}
