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
package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class StickyVerse : HBox() {
    val verseLabelProperty = SimpleStringProperty()
    val resumeTextProperty = SimpleStringProperty()

    init {
        addClass("narration__resume-to-verse")
        hgrow = Priority.ALWAYS

        label(verseLabelProperty) {
            addClass("h4")
        }
        region {
            addClass("narration__resume-to-verse__spacer")
            hgrow = Priority.ALWAYS
        }
        button(resumeTextProperty) {
            addClass("btn", "btn--primary")
            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)

            action {
                FX.eventbus.fire(ResumeVerseEvent())
            }
        }
    }
}

class ResumeVerseEvent : FXEvent()

fun EventTarget.stickyVerse(op: StickyVerse.() -> Unit = {}) =
    StickyVerse().attachTo(this, op) {
    }
