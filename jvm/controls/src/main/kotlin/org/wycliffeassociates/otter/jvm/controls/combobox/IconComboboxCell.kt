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
package org.wycliffeassociates.otter.jvm.controls.combobox

import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon

class IconComboBoxCell<T>(
    private val icon: FontIcon,
    private val convertText: ((item: T) -> String)? = null
) : ListCell<T>() {
    val view = ComboboxButton()
    override fun updateItem(item: T?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = view.apply {
            val text = convertText?.let { it(item) } ?: item.toString()
            textProperty.set(text)
            iconProperty.set(icon)
        }
    }
}
