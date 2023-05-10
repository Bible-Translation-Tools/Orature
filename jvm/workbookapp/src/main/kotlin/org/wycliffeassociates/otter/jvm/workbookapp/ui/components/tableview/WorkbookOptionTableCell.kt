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

import javafx.geometry.Pos
import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.ProjectInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.WorkbookOptionMenu
import tornadofx.*
import tornadofx.FX.Companion.messages

class WorkbookOptionTableCell : TableCell<ProjectInfo, ProjectInfo>() {

    private val popupMenu = WorkbookOptionMenu()

    private val actionButton = button {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
            addClass("wa-icon")
        }
        tooltip(messages["options"])
    }

    private val graphicContent = hbox {
        alignment = Pos.CENTER_RIGHT
        add(actionButton)
    }

    override fun updateItem(item: ProjectInfo?, empty: Boolean) {
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
                    FX.primaryStage
                )
                popupMenu.x = screenBound.centerX - popupMenu.width + this.width
                popupMenu.y = screenBound.maxY
        }

        graphic = graphicContent
    }
}