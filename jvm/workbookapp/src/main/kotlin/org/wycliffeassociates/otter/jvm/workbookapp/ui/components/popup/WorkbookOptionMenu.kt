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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import tornadofx.FX
import tornadofx.action
import tornadofx.addClass
import tornadofx.get

class WorkbookOptionMenu : ContextMenu() {

    val workbookInfoProperty = SimpleObjectProperty<WorkbookDescriptor>(null)

    init {
        val openOption = MenuItem(FX.messages["openBook"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookOpenEvent(it))
                }
            }
        }
        val backupOption = MenuItem(FX.messages["backup"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_CONTENT_DUPLICATE)
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookExportEvent(it))
                }
            }
        }
        val exportOption = MenuItem(FX.messages["exportOptions"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookExportEvent(it))
                }
            }
        }
        val deleteOption = MenuItem(FX.messages["deleteBook"]).apply {
            addClass("danger")
            graphic = FontIcon(MaterialDesign.MDI_DELETE)
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookDeleteEvent(it))
                }
            }
        }
        addClass("wa-context-menu")
        isAutoHide = true
        items.setAll(openOption, backupOption, exportOption, deleteOption)
    }
}