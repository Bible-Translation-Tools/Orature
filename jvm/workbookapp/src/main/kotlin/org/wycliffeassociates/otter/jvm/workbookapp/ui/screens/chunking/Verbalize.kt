package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.animation.Timeline
import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Arc
import javafx.scene.shape.ArcType
import javafx.scene.shape.Circle
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class Verbalize : View() {
    val chunkvm: ChunkingViewModel by inject()
    val vm: VerbalizeViewModel by inject()

    val playIcon = FontIcon("mdi-play").apply {
        iconSize = 36
        iconColor = Color.WHITE
    }
    val pauseIcon = FontIcon("mdi-pause").apply {
        iconSize = 36
        iconColor = Color.WHITE
    }
    val recordIcon = FontIcon("mdi-microphone").apply { iconSize = 48 }
    val stopIcon = FontIcon("mdi-stop").apply { iconSize = 48 }
    val rerecordButton = FontIcon("mdi-sync").apply {
        iconColor = Paint.valueOf("#00377c")
        iconSize = 36
    }

    val arc = Circle().apply {
        fill = c("#001533", .10);
        centerX = 120.0
        centerY = 120.0
        radius = 60.0
        style {
            backgroundColor += Color.TRANSPARENT
        }
    }

    override fun onDock() {
        super.onDock()
        chunkvm.titleProperty.set("Verbalize")
        chunkvm.stepProperty.set("Record a summary of what you heard in the chapter. You may use this later to help remember the story.")
    }

    override val root = borderpane {
        style {
            backgroundColor += Color.WHITE
        }
        center = hbox {
            spacing = 25.0
            alignment = Pos.CENTER

            button {
                visibleProperty().bind(vm.hasContentProperty)
                vm.isPlayingProperty.onChangeAndDoNow {
                    it?.let {
                        when(it) {
                            true -> graphic = pauseIcon
                            false -> graphic = playIcon
                        }
                    }
                }
                styleClass.addAll("btn", "btn--primary")
                action { vm.playToggle() }
                style {
                    prefHeight = 75.px
                    prefWidth = 75.px
                    borderRadius += box(90.px)
                    backgroundRadius += box(90.px)
                }
            }
            stackpane {
                prefWidth = 200.0
                add(arc)
                button {
                    vm.recordingProperty.onChangeAndDoNow {
                        it?.let {
                            when (it) {
                                true -> graphic = stopIcon
                                false -> graphic = recordIcon
                            }
                        }
                    }
                    styleClass.addAll("btn", "btn--cta")
                    action {
                        arc.radiusProperty().bind(vm.arcLengthProperty)
                        vm.toggle()
                    }
                    style {
                        prefHeight = 125.px
                        prefWidth = 125.px
                        borderRadius += box(90.px)
                        backgroundRadius += box(90.px)
                    }
                }
            }
            button {
                visibleProperty().bind(vm.hasContentProperty)
                graphic = rerecordButton
                styleClass.addAll("btn", "btn--secondary")
                action { vm.reRec() }
                style {
                    prefHeight = 75.px
                    prefWidth = 75.px
                    borderRadius += box(90.px)
                    backgroundRadius += box(90.px)
                }
            }

        }
    }
}
