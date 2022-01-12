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
package org.wycliffeassociates.otter.jvm.controls.combobox

import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.ComboBox
import tornadofx.*

/**
 * This class contains a comboBox that is searchable and filterable through a text field.
 * It auto selects any text in the field when refocusing back on the comboBox
 *
 * @author Caleb Benedick
 * @author Matthew Russell
 */
class FilterableComboBox<T> : ComboBox<T>() {
    private val filterItems = FXCollections.observableArrayList<FilterableItem<T>>()
    var filterConverter: (T) -> List<String> = { item -> listOf(item.toString()) }

    init {
        /** Set up filterable comboBox based on the incoming data to select from */
        isEditable = true
        skin = FilterableComboBoxSkin(this) { input ->
            filterItems
                .filter { it.filterText.joinToString("&").contains(input, true) }
                .sortedBy { it.filterText.joinToString("&").indexOf(input, ignoreCase = true) }
                .map { it.item }
        }

        itemsProperty().addListener { _ ->
            items.onChange { _ ->
                refreshFilterItems()
            }
        }
        items.onChange { _ ->
            refreshFilterItems()
        }

        editor.focusedProperty().onChange {
            (skin as FilterableComboBoxSkin<*>).showDropdownIfFocused()
        }
    }

    private fun refreshFilterItems() {
        filterItems.setAll(items.map { FilterableItem(it, filterConverter(it)) })
    }
}

class FilterableComboBoxSkin<T>(comboBox: ComboBox<T>, autoCompleteFilter: ((String) -> List<T>)? = null) :
    AutoCompleteComboBoxSkin<T>(comboBox, autoCompleteFilter, false) {
    fun showDropdownIfFocused() {
        if (editor?.isFocused == true && comboBox.items.isNotEmpty()) {
            // Trigger the dropdown and make sure the items are showing
            listView.items = comboBox.items
            comboBox.show()
        }
    }
}

fun <T> EventTarget.filterablecombobox(
    property: Property<T>? = null,
    values: List<T>? = null,
    init: FilterableComboBox<T>.() -> Unit = {}
): FilterableComboBox<T> = FilterableComboBox<T>().also {
    if (values != null) it.items = (values as? ObservableList<T>) ?: values.asObservable()
    if (property != null) it.bind(property)
}.attachTo(this, init)
