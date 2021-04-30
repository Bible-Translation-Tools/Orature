package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javax.inject.Inject
import javax.inject.Provider
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.audio.wav.WavFileReader
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class Consume : Fragment() {

    val vm: ChunkingViewModel by inject()
    val wkbk: WorkbookDataStore by inject()

    val audioController: AudioPlayerController
    val audioSlider: AudioSlider

    @Inject
    lateinit var audioPlayerProvider: Provider<IAudioPlayer>
    val ap: IAudioPlayer
    val waveformImageBuilder = WaveformImageBuilder(wavColor = Color.web("#00153399"))

    override fun onDock() {
        super.onDock()
        vm.titleProperty.set("Consume")
        vm.stepProperty.set("Listen to the source audio for chapter ${wkbk.chapter.sort}. Pay attention to stories and important events.")
    }

    init {
        (app as OtterApp).dependencyGraph.inject(this)
        ap = audioPlayerProvider.get()
        ap.load(vm.sourceAudio.get().file)

        audioSlider = AudioSlider().apply {
            prefHeightProperty().set(400.0)
            player.set(ap)
            val wav = vm.sourceAudio.get()
            secondsToHighlightProperty.set(0)
            waveformImageBuilder.build(WavFileReader(wav), fitToAudioMax = false).subscribe { img ->
                waveformImageProperty.set(img)
            }
        }

        audioController = AudioPlayerController(audioSlider, ap)
    }

    override val root = vbox {
        importStylesheet(resources["/css/root.css"])
        importStylesheet(resources["/css/button.css"])
        borderpane {
            alignment = Pos.CENTER
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS
            center = audioSlider
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
                    audioController.toggle()
                }
                style {
                    borderRadius += box(25.px)
                    backgroundRadius += box(25.px)
                }
            }
        }
    }
}

