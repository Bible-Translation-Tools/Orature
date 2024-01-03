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

import javafx.beans.property.SimpleIntegerProperty
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.WorkbookOptionMenu
import tornadofx.*
import tornadofx.FX.Companion.messages

class WorkbookOptionTableCell(
    private val selectedIndexProperty: SimpleIntegerProperty,
) : TableCell<WorkbookDescriptor, WorkbookDescriptor>() {
    private val actionButton =
        button {
            addClass("btn", "btn--icon", "btn--borderless", "option-button")
            graphic =
                FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
                    addClass("wa-icon", "option-icon")
                }
            tooltip(messages["options"])

            isFocusTraversable = false
        }

    private val popupMenu =
        WorkbookOptionMenu().apply {
            setOnShowing {
                actionButton.addClass("button--active")
            }
            setOnHidden {
                actionButton.removeClass("button--active")
            }
        }

    private val graphicContent =
        hbox {
            alignment = Pos.CENTER_RIGHT
            add(actionButton)
        }

    init {
        selectedIndexProperty.onChange {
            if (item != null && !isEmpty && it == index) {
                actionButton.onAction.handle(ActionEvent())
                selectedIndexProperty.set(-1) // resets to allow subsequent invocations
            }
        }
    }

    override fun updateItem(
        item: WorkbookDescriptor?,
        empty: Boolean,
    ) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            popupMenu.workbookInfoProperty.set(null)
            graphic = null
            return
        }

        popupMenu.workbookInfoProperty.set(item)
        actionButton.setOnAction {
            val bound = this.boundsInLocal
            val screenBound = this.localToScreen(bound)
            popupMenu.show(
                FX.primaryStage,
            )
            popupMenu.x = screenBound.minX - popupMenu.width + this.width
            popupMenu.y = screenBound.centerY
        }

        graphic = graphicContent
    }
}
