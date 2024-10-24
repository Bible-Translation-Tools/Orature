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

import javafx.geometry.Pos
import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.model.WorkbookDescriptorWrapper
import tornadofx.*

class WorkbookSourceAudioTableCell : TableCell<WorkbookDescriptorWrapper, Boolean>() {

    private val graphicContent = hbox {
        alignment = Pos.CENTER
        add(
            FontIcon(MaterialDesign.MDI_VOLUME_HIGH).apply {
                addClass("active-icon")
            }
        )
    }

    override fun updateItem(item: Boolean?, empty: Boolean) {
        super.updateItem(item, empty)

        graphic = if (item == true) {
            graphicContent
        } else {
            null
        }
    }
}
