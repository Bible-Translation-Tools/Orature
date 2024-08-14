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

import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.controls.model.ResourceVersion
import tornadofx.*
import tornadofx.FX.Companion.messages

class ResourceVersionTableView(
    resources: ObservableList<ResourceVersion>
): TableView<ResourceVersion>(resources) {
    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        placeholder = Region() // shows nothing when table is empty

        column(messages["resource_name"], String::class) {
            addClass("table-view__column-header-row")
            setCellValueFactory { it.value.name.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("h4", "h4--80")
                    tooltip(item)
                }
            }
        }
        column(messages["code"], String::class).apply {
            addClass("table-view__column-header-row")
            setCellValueFactory { it.value.slug.toProperty() }
            cellFormat {
                graphic = label(item) { addClass("normal-text") }
            }
        }

        setRowFactory { ResourceVersionTableRow() }

        //TODO: accessibility support
    }
}

fun EventTarget.resourceVersionTableView(
    values: ObservableList<ResourceVersion>,
    op: ResourceVersionTableView.() -> Unit = {}
) = ResourceVersionTableView(values).attachTo(this, op)