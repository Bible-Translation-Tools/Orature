package org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox

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
    if (values != null) it.items = (values as? ObservableList<T>) ?: values.observable()
    if (property != null) it.bind(property)
}.attachTo(this, init)
