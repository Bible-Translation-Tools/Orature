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

    val vm : RecorderViewModel by inject()

    val timer = label {
        textProperty().bind(vm.timerTextProperty)
    }
    val saveBtn = MaterialIconView(MaterialIcon.CHECK, "48px")
    val recordBtn = MaterialIconView(MaterialIcon.MIC, "48px")

    override val root = borderpane {

        left {
            vbox {
                add(timer)
                timer.apply {
                    fontProperty().set(Font.font("noto sans", 32.0))
                    textFill = Color.WHITE
                }
                alignment = Pos.CENTER_LEFT
                padding = Insets(10.0, 0.0, 10.0, 10.0)
            }
        }
        center {
            hgrow = Priority.ALWAYS
            add(recordBtn)
            recordBtn.apply { fill = Color.WHITE }

            recordBtn.size = "48px"

            recordBtn.setOnMouseClicked {
                if(!vm.isRecording) {
                    recordBtn.setIcon(MaterialIcon.PAUSE_CIRCLE_OUTLINE)
                } else {
                    recordBtn.setIcon(MaterialIcon.MIC)
                }
                vm.toggle()
            }
        }
        right {
            hbox {
                add(saveBtn)
                saveBtn.apply {
                    fill = Color.WHITE
                    setOnMouseClicked {
                        vm.save()
                    }
                }
                saveBtn.visibleProperty().bind(vm.canSaveProperty)
                padding = Insets(10.0, 10.0, 10.0, 0.0)
                alignment = Pos.CENTER_RIGHT
            }
        }
        background = Background(BackgroundFill(Paint.valueOf("#C2185B"), CornerRadii.EMPTY, Insets.EMPTY))
    }
}