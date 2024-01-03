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
package org.wycliffeassociates.otter.jvm.controls.bar

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Button
import org.controlsfx.control.textfield.CustomTextField
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class SearchBar : CustomTextField() {

    private val searchIcon = FontIcon(MaterialDesign.MDI_MAGNIFY)
    private val clearBtn = Button().apply {
        addClass("btn", "btn--borderless", "text-field-button")
        graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
    }

    init {
        addClass("txt-input", "filtered-search-bar__input")

        clearBtn.setOnAction {
            text = ""
            this.requestFocus()
        }
        rightProperty().bind(createGraphicBinding())
    }

    private fun createGraphicBinding(): ObjectBinding<Node> {
        return Bindings.createObjectBinding(
            {
                if (textProperty().isEmpty.value) {
                    searchIcon
                } else {
                    clearBtn
                }
            },
            textProperty()
        )
    }
}

fun EventTarget.searchBar(op: SearchBar.() -> Unit = {}) = SearchBar().attachTo(this, op)
