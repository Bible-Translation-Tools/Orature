package org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox

import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.control.ComboBox
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
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
        makeAutocompletable(false) { input ->
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
            if (it && items.isNotEmpty()) {
                // Trigger the dropdown
                forceShow()
            }
        }
    }

    private fun refreshFilterItems() {
        filterItems.setAll(items.map { FilterableItem(it, filterConverter(it)) })
        if (editor.isFocused && items.isNotEmpty()) forceShow()
    }

    private fun forceShow() {
        if (editor.text.isEmpty()) {
            valueProperty().value = null
            // Change the editor's text so the filter handler thinks it needs to display new suggestions
            editor.text = "a" 
            // Fire a fake key released event to trigger the autocomplete handler
            editor.fireEvent(KeyEvent(
                    KeyEvent.KEY_RELEASED,
                    "a", "a",
                    KeyCode.A,
                    false, false, false, false
            ))
            // Clear the text of the fake 'a'
            editor.clear()
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
