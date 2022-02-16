package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import java.text.MessageFormat
import javafx.animation.AnimationTimer
import javafx.scene.Node
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerPlacementWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerTrackControl
import org.wycliffeassociates.otter.jvm.controls.waveform.ScrollingWaveform
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.button
import tornadofx.get
import tornadofx.hbox

class Chunk : Fragment() {

    val playIcon = FontIcon("mdi-play")
    val pauseIcon = FontIcon("mdi-pause")

    val vm: ChunkingViewModel by inject()

    var timer: AnimationTimer? = null

    override fun onDock() {
        super.onDock()
        tryImportStylesheet(resources.get("/css/verse-marker-app.css"))
        tryImportStylesheet(resources.get("/css/consume-page.css"))
        vm.onDockConsume()
        vm.titleProperty.set("Chunking")
        vm.stepProperty.set(MessageFormat.format(messages["consumeDescription"], vm.chapterTitle))

        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                vm.calculatePosition()
            }
        }
        timer?.start()
    }

    override fun onUndock() {
        super.onUndock()
        timer?.stop()
        vm.compositeDisposable.clear()
    }

    override val root = borderpane {
        center = MarkerPlacementWaveform(HBox()).apply {
            positionProperty.bind(vm.positionProperty)

            vm.compositeDisposable.add(
                vm.waveform.observeOnFx().subscribe { addWaveformImage(it) }
            )

            onWaveformClicked = { vm.pause() }
            onWaveformDragReleased = { deltaPos ->
                val deltaFrames = pixelsToFrames(deltaPos)
                val curFrames = vm.getLocationInFrames()
                val duration = vm.getDurationInFrames()
                val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                vm.seek(final)
            }
        }
        bottom = hbox {
            styleClass.addAll("consume__bottom")
            button {
                vm.isPlayingProperty.onChangeAndDoNow {
                    it?.let {
                        when (it) {
                            true -> graphic = pauseIcon
                            false -> graphic = playIcon
                        }
                    }
                }
                styleClass.addAll("btn", "btn--cta", "consume__btn--primary")
                action {
                    vm.mediaToggle()
                }
            }
        }
    }
}

