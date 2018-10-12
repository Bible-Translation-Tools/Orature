package org.wycliffeassociates.otter.jvm.app.widgets.filterablecombobox

import javafx.beans.property.Property
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.ComboBox
import javafx.scene.layout.Pane
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

        /** Select any text in the editor when it is refocused */
        editor.focusedProperty().onChange {
            if (editor.isFocused && !editor.text.isEmpty()) {
                editor.selectAll()
            }
        }
    }

    private fun refreshFilterItems() {
        filterItems.setAll(
                items.map { FilterableItem(it, filterConverter(it)) }
        )
    }
}

fun <T> EventTarget.filterablecombobox(
        property: Property<T>? = null,
        values: List<T>? = null,
        init: FilterableComboBox<T>.() -> Unit = {}
): FilterableComboBox<T> {
    val box = FilterableComboBox<T>()
    box.init()
    if (values != null) box.items = (values as? ObservableList<T>) ?: values.observable()
    if (property != null) box.bind(property)
    add(box)
    return box
}
