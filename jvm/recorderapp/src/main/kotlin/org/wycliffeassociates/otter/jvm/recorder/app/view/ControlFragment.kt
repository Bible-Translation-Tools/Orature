package org.wycliffeassociates.otter.jvm.recorder.app.view

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import tornadofx.*

class ControlFragment : Fragment() {

    val vm: RecorderViewModel by inject()

    val timer = label {
        textProperty().bind(vm.timerTextProperty)
    }
    val saveBtn = MaterialIconView(MaterialIcon.CHECK, "48px")
    val recordBtn = MaterialIconView(MaterialIcon.MIC, "48px")

    override val root = borderpane {
        background = Background(BackgroundFill(Paint.valueOf("#C2185B"), CornerRadii.EMPTY, Insets.EMPTY))

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
                add(saveBtn)
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

        saveBtn.apply {
            fill = Color.WHITE
            visibleProperty().bind(vm.canSaveProperty)
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