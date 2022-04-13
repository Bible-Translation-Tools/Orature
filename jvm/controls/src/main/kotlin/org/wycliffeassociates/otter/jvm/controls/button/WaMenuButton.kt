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
package org.wycliffeassociates.otter.jvm.controls.button

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class WaMenuButton : Button() {
    val buttonTextProperty = SimpleStringProperty()
    val sideProperty = SimpleObjectProperty<Side>()
    val items: ObservableList<MenuItem> = FXCollections.observableArrayList()

    private var menu: ContextMenu? = null

    init {
        addClass("wa-menu-button")

        graphic = HBox().apply {
            addClass("wa-menu-button__content")

            label {
                addClass("wa-menu-button__icon")
                graphic = FontIcon(MaterialDesign.MDI_SPEEDOMETER)
            }
            label {
                textProperty().bind(buttonTextProperty)
            }
            label {
                addClass("wa-menu-button__arrow")
                graphic = FontIcon(MaterialDesign.MDI_TRIANGLE)
            }
        }

        action {
            toggle()
        }

        items.onChange {
            hide()
            if (it.list.isNotEmpty()) {
                menu = ContextMenu()
                menu?.items?.setAll(it.list)
                menu?.setOnShown {
                    menu?.skin?.node?.lookup(".button")?.requestFocus()
                }
            }
        }
    }

    fun show() {
        menu?.show(this, sideProperty.value, 0.0, 0.0)
    }

    fun hide() {
        menu?.hide()
    }

    private fun toggle() {
        when (menu?.isShowing) {
            true -> hide()
            else -> show()
        }
    }
}
