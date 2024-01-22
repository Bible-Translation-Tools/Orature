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
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Callback
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class LanguageTableView(
    languages: ObservableList<Language>
) : TableView<Language>(languages) {

    val disabledLanguages = observableListOf<Language>()

    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY
        placeholder = Region() // shows nothing when table is empty

        column(messages["language"], String::class) {
            setCellValueFactory { it.value.name.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("h4", "h4--80")
                    tooltip(text)
                }
            }
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
        }
        column(messages["anglicized"], String::class) {
            setCellValueFactory { it.value.anglicizedName.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("normal-text")
                    tooltip(text)
                }
            }
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
        }
        column(messages["code"], String::class) {
            setCellValueFactory { it.value.slug.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("normal-text")
                }
            }
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
        }
        column(messages["gateway"], Boolean::class) {
            setCellValueFactory { it.value.isGateway.toProperty() }
            cellFormat {
                graphic = label {
                    text = if (item) messages["yes"] else messages["no"]
                    addClass("normal-text")
                }
            }
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
        }

        sortPolicy = CUSTOM_SORT_POLICY as (Callback<TableView<Language>, Boolean>)
        setRowFactory { LanguageTableRow(disabledLanguages) }

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
                selectedItem?.let { language ->
                    if (selectedItem !in disabledLanguages) {
                        FX.eventbus.fire(LanguageSelectedEvent(language))
                    }
                }
                keyEvent.consume()
            }
        }

        bindTableSortComparator()
        runLater { customizeScrollbarSkin() }
    }
}

/**
 * Constructs a language table and attach it to the parent.
 */
fun EventTarget.languageTableView(
    values: ObservableList<Language>,
    op: LanguageTableView.() -> Unit = {}
) = LanguageTableView(values).attachTo(this, op)
