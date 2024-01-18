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

import io.github.palexdev.materialfx.controls.MFXProgressBar
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Callback
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import tornadofx.FX.Companion.messages

class WorkBookTableView(
    books: ObservableList<WorkbookDescriptor>
) : TableView<WorkbookDescriptor>(books) {

    private val selectedIndexProperty = SimpleIntegerProperty(-1)

    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY
        placeholder = Region() // shows nothing when table is empty

        column(messages["book"], String::class) {
            addClass("table-view__column-header-row")
            setCellValueFactory { it.value.title.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("h4", "h4--80")
                    tooltip(item)
                }
            }
            prefWidthProperty().bind(this@WorkBookTableView.widthProperty().multiply(0.25))
            minWidth = 120.0 // this may not be replaced with css
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
        }
        column(messages["code"], String::class).apply {
            addClass("table-view__column-header-row")
            setCellValueFactory { it.value.slug.toProperty() }
            cellFormat {
                graphic = label(item) { addClass("normal-text") }
            }
            minWidth = 80.0 // this may not be replaced with css
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
        }
        column(messages["anthology"], String::class).apply {
            addClass("table-view__column-header-row")
            setCellValueFactory { it.value.anthology.titleKey.toProperty() }
            cellFormat {
                graphic = label {
                    text = if (item.isEmpty()) "" else messages[item] // catch empty string for key
                    addClass("h5")
                }
            }
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
        }
        column(messages["progress"], Number::class) {
            setCellValueFactory { it.value.progress.toProperty() }
            cellFormat {
                val percent = item.toDouble()
                graphic = MFXProgressBar(percent).apply {
                    fitToParentWidth()
                }
            }
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
        }
        column("", Boolean::class) {
            addClass("table-column__status-icon-col")
            setCellValueFactory { SimpleBooleanProperty(it.value.hasSourceAudio) }
            setCellFactory { WorkbookSourceAudioTableCell() }
            isReorderable = false
            isSortable = true
            minWidth = 80.0
            maxWidth = 100.0
            bindColumnSortComparator()
        }
        column("", WorkbookDescriptor::class) {
            setCellValueFactory { SimpleObjectProperty(it.value) }
            setCellFactory {
                WorkbookOptionTableCell(selectedIndexProperty)
            }
            minWidth = 80.0
            maxWidth = 100.0
            isReorderable = false
            isSortable = false
        }

        sortPolicy = CUSTOM_SORT_POLICY as (Callback<TableView<WorkbookDescriptor>, Boolean>)
        setRowFactory {
            WorkbookTableRow()
        }

        /* accessibility */
        focusedProperty().onChange {
            if (it && selectionModel.selectedIndex < 0) {
                selectionModel.select(0)
                focusModel.focus(0)
            }
        }

        /* accessibility */
        addEventFilter(KeyEvent.KEY_PRESSED) { keyEvent ->
            if (keyEvent.code == KeyCode.SPACE || keyEvent.code == KeyCode.ENTER) {
                selectedIndexProperty.set(selectionModel.selectedIndex)
                keyEvent.consume()
            }
        }

        handleDefaultSortOrder()
        runLater { customizeScrollbarSkin() }
    }
}

private fun WorkBookTableView.handleDefaultSortOrder() {
    val list = this.items
    if (list is SortedList<WorkbookDescriptor>) {
        comparatorProperty().onChangeAndDoNow {
            if (sortOrder.isEmpty()) {
                // when toggled to "unsorted", resets to default order (usually Biblical order)
                list.comparator = Comparator { wb1, wb2 -> wb1.sort.compareTo(wb2.sort) }
            } else {
                list.comparator = it
            }
        }
    }
}

/**
 * Constructs a workbook table and attach it to the parent.
 */
fun EventTarget.workbookTableView(
    values: ObservableList<WorkbookDescriptor>,
    op: WorkBookTableView.() -> Unit = {}
) = WorkBookTableView(values).attachTo(this, op)
