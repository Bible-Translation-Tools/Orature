/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo
import tornadofx.*
import tornadofx.FX.Companion.messages

class WorkBookTableView(
    books: ObservableList<WorkbookInfo>
) : TableView<WorkbookInfo>(books) {

    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        column(messages["book"], String::class) {
            addClass("table-view__column-header-row")
            setCellValueFactory { it.value.title.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("h4", "h4--80")
                }
            }
            isReorderable = false
        }
        column(messages["progress"], Number::class) {
            setCellValueFactory { it.value.progress.toProperty() }
            cellFormat {
                val percent = item.toDouble()
                graphic = progressbar(percent) {
                    if (percent == 1.0) addClass("full")
                }
            }
            isReorderable = false
        }
        column("", Boolean::class) {
            addClass("table-column__status-icon-col")
            setCellValueFactory { SimpleBooleanProperty(it.value.hasSourceAudio) }
            cellFormat {
                graphic = if (it) {
                    FontIcon(MaterialDesign.MDI_VOLUME_HIGH).apply {
                        addClass("active-icon")
                    }
                } else {
                    null
                }
            }

            maxWidth = 50.0
            minWidth = 50.0
            isReorderable = false
            isResizable = false
            isSortable = false
        }
        column("", WorkbookInfo::class) {
            setCellValueFactory { SimpleObjectProperty(it.value) }
            setCellFactory {
                WorkbookOptionTableCell()
            }

            maxWidth = 100.0
            minWidth = 80.0
            isReorderable = false
            isResizable = false
            isSortable = false
        }

        setRowFactory {
            WorkbookTableRow()
        }
    }
}

/**
 * Constructs a workbook table and attach it to the parent.
 */
fun EventTarget.workbookTableView(
    values: ObservableList<WorkbookInfo>,
    op: WorkBookTableView.() -> Unit = {}
) = WorkBookTableView(values).attachTo(this, op)
