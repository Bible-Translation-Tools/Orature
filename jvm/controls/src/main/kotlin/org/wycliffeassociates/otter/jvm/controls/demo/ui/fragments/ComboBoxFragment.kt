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
package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.combobox.ComboboxItem
import org.wycliffeassociates.otter.jvm.controls.combobox.IconComboBoxCell
import tornadofx.*

class ComboBoxFragment : Fragment() {
    private val selected = SimpleStringProperty("Item1")
    private val items = FXCollections.observableArrayList("Item1", "Item2", "Item3")

    override val root = stackpane {
        combobox(selected, items) {
            addClass("wa-combobox")

            cellFormat {
                val view = ComboboxItem()
                graphic = view.apply {
                    topTextProperty.set(it)
                }
            }
            buttonCell = IconComboBoxCell(FontIcon(MaterialDesign.MDI_ACCOUNT))
        }
    }
}