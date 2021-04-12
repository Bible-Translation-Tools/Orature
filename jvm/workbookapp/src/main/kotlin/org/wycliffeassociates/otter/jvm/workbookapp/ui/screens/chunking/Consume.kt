package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Paint
import javax.inject.Inject
import javax.inject.Provider
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.audio.wav.WavFileReader
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import tornadofx.*

class Consume : Fragment() {

    val vm: ChunkingViewModel by inject()

    @Inject
    lateinit var audioPlayerProvider: Provider<IAudioPlayer>
    lateinit var ap: IAudioPlayer
    val waveformImageBuilder = WaveformImageBuilder()

    init {
        (app as OtterApp).dependencyGraph.inject(this)
        ap = audioPlayerProvider.get()
        ap.load(vm.sourceAudio.get().file)
    }

    override val root = vbox {
        importStylesheet(resources["/css/root.css"])
        importStylesheet(resources["/css/button.css"])
        hbox {
            padding = insets(20.0)
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            add(
                AudioSlider().apply {
                    hgrow = Priority.ALWAYS
                    this.player.set(ap)
                    val wav = vm.sourceAudio.get()
                    secondsToHighlightProperty.set(0)
                    waveformImageBuilder.build(WavFileReader(wav)).subscribe { img ->
                        waveformImageProperty.set(img)
                    }
                }
            )
        }
        hbox {
            prefHeight = 74.0
            alignment = Pos.CENTER
            style {
                backgroundColor += Paint.valueOf("#015AD9")
            }
            button("", FontIcon("mdi-play")) {
                styleClass.addAll("btn", "btn--cta")
                action {
                    if (ap.isPlaying()) {
                        ap.pause()
                    } else {
                        ap.play()
                    }
                }
            }
        }
    }
}

