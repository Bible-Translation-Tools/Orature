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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.collections.FXCollections
import javafx.collections.transformation.SortedList
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.util.Callback
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

/**
 * Custom implementation of javafx DEFAULT_SORT_POLICY callback.
 * This allows both manual sorting when then user clicks on the column
 * and automatic sorting from the search filter.
 *
 * @see javafx.scene.control.TableView.DEFAULT_SORT_POLICY
 */
internal val CUSTOM_SORT_POLICY: Callback<TableView<Any>, Boolean> = Callback { table ->
    try {
        val itemsList = table.items
        if (itemsList is SortedList<Any> || itemsList == null || itemsList.isEmpty()) {
            return@Callback true
        } else {
            val comparator = table.comparator ?: return@Callback true
            FXCollections.sort(itemsList, comparator)
            return@Callback true
        }
    } catch (e: UnsupportedOperationException) {
        return@Callback false
    }
}

/**
 * Updates the sort comparator when the user toggles between sorting types on the column.
 * Use this method to allow sorting the table from the back-end, given the underlying
 * table data is backed by a SortedList<T>.
 *
 * This method should be used in conjunction with [bindTableSortComparator] to handle
 * the different states of the column sort.
 */
fun <S, T> TableColumn<S, T>.bindColumnSortComparator() {
    val list = tableView.items
    sortTypeProperty().onChange {
        if (list is SortedList<S>) {
            list.comparator = tableView.comparator
        }
    }
}

/**
 * Updates the backing sorted list comparator when the table comparator changes.
 * This will handle the transition to "unsorted" state of the columns.
 *
 * Use this method in conjunction with [bindColumnSortComparator] to handle
 * the different states of the column sort.
 */
fun <S> TableView<S>.bindTableSortComparator() {
    val list = this.items
    if (list is SortedList<S>) {
        comparatorProperty().onChangeAndDoNow {
            list.comparator = it
        }
    }
}
