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
package org.wycliffeassociates.otter.jvm.controls.dialog

import io.github.palexdev.materialfx.controls.MFXProgressBar
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class ProgressDialog : OtterDialog() {
    
    val dialogTitleProperty = SimpleStringProperty()
    val dialogMessageProperty = SimpleStringProperty()
    val percentageProperty = SimpleDoubleProperty(0.0)
    val progressMessageProperty = SimpleStringProperty()
    val cancelMessageProperty = SimpleStringProperty()
    val allowCloseProperty = SimpleBooleanProperty(true)

    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    
    private val content = VBox().apply {
        addClass("confirm-dialog", "progress-dialog")

        hbox {
            addClass("confirm-dialog__header")
            label {
                textProperty().bind(dialogTitleProperty)
                addClass("h3")
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--icon", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                tooltip(messages["close"])
                visibleWhen { allowCloseProperty }
                managedWhen(visibleProperty())
                onActionProperty().bind(onCloseActionProperty)
            }
        }
        vbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS
            label {
                addClass("confirm-dialog__message")
                textProperty().bind(dialogMessageProperty)
            }
            add(
                MFXProgressBar().apply {
                    prefWidthProperty().bind(this@vbox.widthProperty())
                    progressProperty().bind(percentageProperty.divide(100))
                }
            )
            hbox {
                label {
                    addClass("h5")
                    textProperty().bind(progressMessageProperty)
                }
                region { hgrow = Priority.ALWAYS }
                label {
                    addClass("normal-text")
                    textProperty().bind(percentageProperty.stringBinding {
                        String.format("%.0f%%", it ?: 0.0)
                    })
                }
            }
        }
        hbox {
            addClass("confirm-dialog__footer")
            region { hgrow = Priority.ALWAYS }
            button(cancelMessageProperty) {
                addClass("btn", "btn--secondary")
                tooltip { textProperty().bind(cancelMessageProperty) }
                onActionProperty().bind(onCloseActionProperty)
                visibleWhen { cancelMessageProperty.isNotNull }
                managedWhen(visibleProperty())
            }
        }
    }

    init {
        setContent(content)
    }

    fun setOnCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op() })
    }
}

fun EventTarget.progressDialog(op: ProgressDialog.() -> Unit) = ProgressDialog().apply(op)