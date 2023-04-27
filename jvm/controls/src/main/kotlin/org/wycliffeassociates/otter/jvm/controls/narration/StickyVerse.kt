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
import javafx.beans.property.SimpleBooleanProperty
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

class StickyVerse : VBox() {
    val verseLabelProperty = SimpleStringProperty()
    val onResumeVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val resumeTextProperty = SimpleStringProperty()

    init {
        addClass("narration__selected-verse")

        hbox {
            addClass("narration__selected-verse-controls")

            label {
                textProperty().bind(verseLabelProperty)
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button(resumeTextProperty) {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                onActionProperty().bind(onResumeVerseActionProperty)
            }
        }

        managedProperty().bind(visibleProperty())
    }

    fun onResumeVerse(op: () -> Unit) {
        onResumeVerseActionProperty.set(EventHandler { op() })
    }
}

fun EventTarget.stickyVerse(op: StickyVerse.() -> Unit = {}) =
    StickyVerse().attachTo(this, op) {
    }
