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
        if (itemsList is SortedList<Any>) {
            return@Callback true
        } else {
            if (itemsList == null || itemsList.isEmpty()) {
                // sorting is not supported on null or empty lists
                return@Callback true
            }
            val comparator = table.comparator ?: return@Callback true

            // otherwise we attempt to do a manual sort, and if successful
            // we return true
            FXCollections.sort(itemsList, comparator)
            return@Callback true
        }
    } catch (e: UnsupportedOperationException) {
        return@Callback false
    }
}

/**
 * Updates the comparator when the user toggles sorting type on the column.
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
 * Update the backing sorted list comparator when the table comparator changes.
 * This handles the transition to "unsorted" state of the columns.
 */
fun <S> TableView<S>.bindTableSortComparator() {
    val list = this.items
    if (list is SortedList<S>) {
        comparatorProperty().onChangeAndDoNow {
            list.comparator = it
        }
    }
}
