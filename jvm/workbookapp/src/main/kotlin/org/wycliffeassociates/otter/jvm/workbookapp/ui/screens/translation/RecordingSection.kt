/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.canvas.CanvasFragment
import tornadofx.*
import tornadofx.FX.Companion.messages

class RecordingSection : BorderPane() {

    val isRecordingProperty = SimpleBooleanProperty(false)
    private val toggleRecordingProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val saveActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val cancelActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
    private val resumeIcon = FontIcon(MaterialDesign.MDI_MICROPHONE)

    val waveformCanvas = CanvasFragment()
    val volumeCanvas = CanvasFragment().apply { minWidth = 25.0 }

    init {
        center = waveformCanvas
        right = volumeCanvas
        bottom = hbox {
            addClass("consume__bottom", "recording__bottom-section")
            button {
                addClass("btn", "btn--primary", "consume__btn")
                textProperty().bind(isRecordingProperty.stringBinding {
                    togglePseudoClass("active", it == true)
                    if (it == true) {
                        graphic = pauseIcon
                        messages["pause"]
                    } else {
                        graphic = resumeIcon
                        messages["resume"]
                    }
                })
                tooltip { textProperty().bind(this@button.textProperty()) }

                action {
                    toggleRecordingProperty.value?.handle(ActionEvent())
                }
            }
            button(messages["save"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE)

                visibleWhen { isRecordingProperty.not() }
                managedWhen(visibleProperty())

                action {
                    saveActionProperty.value?.handle(ActionEvent())
                }
            }
            region { hgrow = Priority.ALWAYS }
            button(messages["cancel"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)

                visibleWhen { isRecordingProperty.not() }
                managedWhen(visibleProperty())

                action {
                    cancelActionProperty.value?.handle(ActionEvent())
                }
            }
        }
    }

    fun setToggleRecordingAction(op: () -> Unit) {
        toggleRecordingProperty.set(
            EventHandler { op() }
        )
    }

    fun setSaveAction(op: () -> Unit) {
        saveActionProperty.set(
            EventHandler { op() }
        )
    }

    fun setCancelAction(op: () -> Unit) {
        cancelActionProperty.set(
            EventHandler { op() }
        )
    }
}
