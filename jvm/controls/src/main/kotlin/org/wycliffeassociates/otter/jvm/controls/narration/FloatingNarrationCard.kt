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
package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
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
import java.text.MessageFormat

class FloatingNarrationCard : VBox() {
    val floatingLabelProperty = SimpleStringProperty()
    val onFloatingChunkActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val currentChunkTextProperty = SimpleStringProperty()
    val resumeTextProperty = SimpleStringProperty()

    init {
        addClass("narration__selected-verse")

        hbox {
            addClass("narration__selected-verse-controls")

            label {
                textProperty().bind(currentChunkTextBinding())
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button(resumeTextProperty) {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                onActionProperty().bind(onFloatingChunkActionProperty)
            }
        }

        visibleProperty().bind(floatingLabelProperty.isNotNull)
        managedProperty().bind(visibleProperty())
    }

    private fun currentChunkTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                if (currentChunkTextProperty.value != null && floatingLabelProperty.value != null) {
                    MessageFormat.format(currentChunkTextProperty.value, floatingLabelProperty.value)
                } else {
                    ""
                }
            },
            floatingLabelProperty,
            currentChunkTextProperty
        )
    }
}

fun EventTarget.floatingnarrationcard(op: FloatingNarrationCard.() -> Unit = {}) =
    FloatingNarrationCard().attachTo(this, op) {
    }
