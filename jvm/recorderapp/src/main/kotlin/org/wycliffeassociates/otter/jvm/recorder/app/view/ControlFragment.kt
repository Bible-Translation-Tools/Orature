package org.wycliffeassociates.otter.jvm.recorder.app.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import tornadofx.*

class ControlFragment : Fragment() {

    val vm: RecorderViewModel by inject()

    val timer = label {
        textProperty().bind(vm.timerTextProperty)
    }
    val continueBtn = button(messages["continue"])
    val cancelBtn = button(messages["cancel"])
    val recordBtn = MaterialIconView(MaterialIcon.MIC, "48px")

    override val root = borderpane {
        addClass("controls")

        left {
            vbox {
                add(timer)
                alignment = Pos.CENTER_LEFT
                padding = Insets(10.0, 0.0, 10.0, 10.0)
            }
        }
        center {
            hgrow = Priority.ALWAYS
            add(recordBtn)
        }
        right {
            hbox {
                padding = Insets(10.0, 10.0, 10.0, 0.0)
                alignment = Pos.CENTER_RIGHT
                add(continueBtn.apply {
                    graphic = MaterialIconView(MaterialIcon.CHECK, "32px")
                })
                add(cancelBtn.apply {
                    graphic = MaterialIconView(MaterialIcon.UNDO, "32px")
                })
            }
        }
    }

    init {
        timer.apply {
            fontProperty().set(Font.font("noto sans", 32.0))
            textFill = Color.WHITE
        }

        recordBtn.apply {
            fill = Color.WHITE
            setOnMouseClicked {
                toggleRecording()
            }
        }

        continueBtn.apply {
            addClass("continue-button")
            visibleProperty().bind(vm.canSaveProperty)
            managedProperty().bind(vm.recordingProperty.or(vm.hasWrittenProperty))
            setOnMouseClicked {
                vm.save()
            }
        }

        cancelBtn.apply {
            addClass("continue-button")
            visibleProperty().bind(vm.recordingProperty.not().and(vm.hasWrittenProperty.not()))
            managedProperty().bind(vm.recordingProperty.not().and(vm.hasWrittenProperty.not()))
            setOnMouseClicked {
                vm.save()
            }
        }
    }

    private fun toggleRecording() {
        if (!vm.isRecording) {
            recordBtn.setIcon(MaterialIcon.PAUSE_CIRCLE_OUTLINE)
        } else {
            recordBtn.setIcon(MaterialIcon.MIC)
        }
        vm.toggle()
    }
}