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
package org.wycliffeassociates.otter.jvm.controls.listview

import com.jfoenix.controls.JFXTextField
import javafx.beans.property.Property
import javafx.event.EventTarget
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.controls.styles.SearchableListStyles
import tornadofx.*

class SearchableList<T>(listItems: SortedFilteredList<T>, outputValue: Property<T>, auto: Boolean = false) : VBox() {
    var autoSelect: Boolean by property(auto)
    fun autoSelectProperty() = getProperty(SearchableList<T>::autoSelect)

    var value: T by property()
    fun valueProperty() = getProperty(SearchableList<T>::value)

    var searchField: JFXTextField by singleAssign()
    var listView: ListView<T> by singleAssign()

    init {
        importStylesheet<SearchableListStyles>()
        addClass(SearchableListStyles.searchableList)
        outputValue.bindBidirectional(valueProperty())
        hbox {
            addClass(SearchableListStyles.searchFieldContainer)
            add(SearchableListStyles.searchIcon(24).apply { addClass(SearchableListStyles.icon) })
            searchField = JFXTextField()
            searchField.addClass(SearchableListStyles.searchField)
            searchField.focusColor = Color.TRANSPARENT
            searchField.hgrow = Priority.ALWAYS
            add(searchField)
        }
        listView = listview(listItems) {
            addClass(SearchableListStyles.searchListView)
            multiSelect(false)
            valueProperty().bind(selectionModel.selectedItemProperty())
            searchField.textProperty().onChange { _ ->
                refreshSearch(autoSelectProperty().value)
            }
        }
        listItems.bindTo(listView)
    }

    fun refreshSearch(autoselect: Boolean) {
        if (autoselect && listView.items.isNotEmpty()) listView.selectionModel.selectFirst()
    }
}

fun <T> EventTarget.searchablelist(
    listItems: SortedFilteredList<T>,
    value: Property<T>,
    init: SearchableList<T>.() -> Unit
) = SearchableList(listItems, value).attachTo(this, init)
