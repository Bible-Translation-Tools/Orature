package org.wycliffeassociates.otter.jvm.workbookapp

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javafx.scene.control.Slider
import javafx.scene.layout.Priority
import javax.sound.sampled.AudioSystem
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.DEFAULT_AUDIO_FORMAT
import tornadofx.*

fun main() {
    launch<AudioTest>()
}

class AudioTest: App(AudioTestView::class)

class AudioTestView : View() {

    val vm: AudioTestViewModel by inject()

    override val root = hbox {
        button("toggle") {
            setOnAction {
                vm.toggle()
            }
        }
        add(vm.slider)
        combobox(values = listOf("Slow", "Normal", "Fast")) {  }
    }
}

class AudioTestViewModel: ViewModel() {

    val audioFile = File("/Users/joe/Desktop/recording.wav")
    val acf = AudioConnectionFactory()
    val player: IAudioPlayer
    val audioController: AudioPlayerController
    val slider = Slider().apply { hgrow = Priority.ALWAYS }
    val isPlaying = AtomicBoolean(false)

    init {
        acf.setOutputLine(AudioSystem.getSourceDataLine(DEFAULT_AUDIO_FORMAT))
        player = acf.getPlayer()
        player.load(audioFile)
        slider.max = player.getDurationInFrames().toDouble()
        audioController = AudioPlayerController(slider, player)
    }

    fun toggle() {
        if (isPlaying.get()) {
            isPlaying.set(false)
            player.pause()
        } else {
            isPlaying.set(true)
            player.play()
        }
    }
}
