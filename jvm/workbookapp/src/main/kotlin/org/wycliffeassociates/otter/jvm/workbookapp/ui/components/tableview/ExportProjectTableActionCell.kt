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

import javafx.collections.ObservableSet
import javafx.scene.control.CheckBox
import javafx.scene.control.TableCell
import org.wycliffeassociates.otter.jvm.controls.model.ChapterDescriptor
import tornadofx.*

class ExportProjectTableActionCell(
    private val selectedChapters: ObservableSet<ChapterDescriptor>
) : TableCell<ChapterDescriptor, ChapterDescriptor>() {

    private val graphicNode = CheckBox().apply {
        addClass("wa-checkbox")
        isMouseTransparent = true
        isFocusTraversable = false
    }

    override fun updateItem(item: ChapterDescriptor?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            graphicNode.selectedProperty().unbind()
            return
        }

        graphic = graphicNode.apply {
            isDisable = !item.selectable
            isVisible = item.selectable

            selectedProperty().bind(
                booleanBinding(selectedChapters) {
                    selectedChapters.contains(item)
                }
            )
        }
    }
}