/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.recorder.app.view

import com.jfoenix.controls.JFXButton
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import tornadofx.*

class ControlFragment : Fragment() {

    val vm: RecorderViewModel by inject()

    val timer = label {
        textProperty().bind(vm.timerTextProperty)
    }
    val continueBtn = button(messages["continue"], FontIcon("fas-check"))
    val cancelBtn = button(messages["cancel"], FontIcon("gmi-undo"))
    val recordBtn = JFXButton()

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
                add(continueBtn)
                add(cancelBtn)
            }
        }
    }

    init {
        timer.apply {
            fontProperty().set(Font.font("noto sans", 32.0))
            textFill = Color.WHITE
        }

        recordBtn.apply {
            graphic = FontIcon("gmi-mic").apply {
                iconSize = 48
                fill = Color.WHITE
            }
            tooltip {
                textProperty().bind(
                    vm.recordingProperty.stringBinding {
                        if (it == true) messages["pause"] else messages["record"]
                })
            }
            setOnMouseClicked {
                toggleRecording()
            }
        }

        continueBtn.apply {
            addClass("continue-button")
            tooltip(text)
            visibleProperty().bind(vm.canSaveProperty)
            managedProperty().bind(vm.recordingProperty.or(vm.hasWrittenProperty))
            setOnMouseClicked {
                vm.save()
            }
        }

        cancelBtn.apply {
            addClass("continue-button")
            tooltip(text)
            visibleProperty().bind(vm.recordingProperty.not().and(vm.hasWrittenProperty.not()))
            managedProperty().bind(vm.recordingProperty.not().and(vm.hasWrittenProperty.not()))
            setOnMouseClicked {
                vm.save()
            }
        }
    }

    private fun toggleRecording() {
        if (!vm.isRecording) {
            (recordBtn.graphic as FontIcon).iconLiteral = "gmi-pause-circle-outline"
        } else {
            (recordBtn.graphic as FontIcon).iconLiteral = "gmi-mic"
        }
        vm.toggle()
    }
}
