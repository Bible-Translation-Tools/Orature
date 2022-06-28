/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.recorder.app.viewmodel.RecorderViewModel
import tornadofx.*

class ControlFragment : Fragment() {

    private val vm: RecorderViewModel by inject()

    private val timer = label {
        textProperty().bind(vm.timerTextProperty)
    }
    private val continueBtn = button(messages["continue"], FontIcon("fas-check"))
    private val cancelBtn = button(messages["cancel"], FontIcon("gmi-undo"))
    private val recordBtn = Button()
    private val resetBtn = Button(messages["reset"], FontIcon("gmi-delete"))

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
                spacing = 10.0
                add(resetBtn)
                add(continueBtn)
                add(cancelBtn)
            }
        }
    }

    init {
        timer.apply {
            addClass("timer")
        }

        recordBtn.apply {
            addClass("btn", "btn--icon", "btn--borderless", "record-button")
            graphic = FontIcon("gmi-mic")
            tooltip {
                textProperty().bind(
                    vm.recordingProperty.stringBinding {
                        if (it == true) messages["pause"] else messages["record"]
                })
            }
            setOnAction {
                toggleRecording()
            }
            shortcut(Shortcut.RECORD.value)
        }

        resetBtn.apply {
            addClass("btn", "btn--secondary", "reset-button")
            prefHeightProperty().bind(continueBtn.heightProperty())
            tooltip(text)
            visibleWhen { vm.canSaveProperty }

            setOnAction {
                vm.reset()
            }
        }

        continueBtn.apply {
            addClass("btn", "btn--primary", "btn--borderless", "continue-button")
            tooltip(text)
            visibleProperty().bind(vm.canSaveProperty)
            managedProperty().bind(vm.recordingProperty.or(vm.hasWrittenProperty))
            setOnAction {
                vm.save()
            }
            shortcut(Shortcut.GO_BACK.value)
        }

        cancelBtn.apply {
            addClass("btn", "btn--primary", "btn--borderless", "continue-button")
            tooltip(text)
            visibleProperty().bind(vm.recordingProperty.not().and(vm.hasWrittenProperty.not()))
            managedProperty().bind(visibleProperty())
            setOnAction {
                vm.save()
            }
            shortcut(Shortcut.GO_BACK.value)
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
