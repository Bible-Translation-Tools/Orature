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
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportDialogOpenEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookOpenEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookQuickBackupEvent
import tornadofx.FX
import tornadofx.action
import tornadofx.addClass
import tornadofx.get
import tornadofx.tooltip

class WorkbookOptionMenu : ContextMenu() {

    val workbookInfoProperty = SimpleObjectProperty<WorkbookDescriptor>(null)

    init {
        val openOption = MenuItem().apply {
            graphic = Label(FX.messages["openBook"]).apply {
                this.graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                tooltip(text)
            }
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookOpenEvent(it))
                }
            }
        }
        val backupOption = MenuItem().apply {
            graphic = Label(FX.messages["backup"]).apply {
                this.graphic = FontIcon(MaterialDesign.MDI_CONTENT_DUPLICATE)
                tooltip(text)
            }
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookQuickBackupEvent(it))
                }
            }
        }
        val exportOption = MenuItem().apply {
            graphic = Label(FX.messages["exportOptions"]).apply {
                this.graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                tooltip(text)
            }
            action {
                workbookInfoProperty.value?.let {
                    FX.eventbus.fire(WorkbookExportDialogOpenEvent(it))
                }
            }
        }
        val deleteOption = MenuItem().apply {
            addClass("danger")
            graphic = Label(FX.messages["deleteBook"]).apply {
                this.graphic = FontIcon(MaterialDesign.MDI_DELETE)
                tooltip(text)
            }
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