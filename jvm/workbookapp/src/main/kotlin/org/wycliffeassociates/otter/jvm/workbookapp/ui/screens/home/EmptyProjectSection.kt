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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home

import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class EmptyProjectSection : VBox() {
    init {
        addClass("homepage__main-region")

        hbox {
            addClass("homepage__main-region__header-section")
            button {
                addClass("btn", "btn--icon", "btn--tertiary", "option-button")
                graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)
            }
            label(FX.messages["home"]) { addClass("h4") }

        }

        vbox {
            addClass("homepage__main-region__body", "homepage__main-region__empty-section")
            vgrow = Priority.ALWAYS

            label {
                graphic = FontIcon(MaterialDesign.MDI_LIBRARY_BOOKS).apply {
                    addClass("icon-xl")
                }
            }
            label(FX.messages["createProjectMessageTitle"]) {
                addClass("h4", "h4--80")
            }
            region { addClass("line-break") }
            label(FX.messages["createProjectMessageBody"]) {
                addClass("normal-text")
            }
        }
    }
}

fun EventTarget.emptyProjectSection(op: EmptyProjectSection.() -> Unit = {}) = EmptyProjectSection().attachTo(this, op)
