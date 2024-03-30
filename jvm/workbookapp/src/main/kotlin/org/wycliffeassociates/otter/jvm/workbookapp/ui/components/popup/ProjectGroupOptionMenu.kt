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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.controls.event.ProjectContributorsEvent
import org.wycliffeassociates.otter.jvm.controls.event.ProjectGroupDeleteEvent
import org.wycliffeassociates.otter.jvm.controls.model.WorkbookDescriptorWrapper
import tornadofx.FX.Companion.messages
import tornadofx.*

class ProjectGroupOptionMenu : ContextMenu() {
    val books = observableListOf<WorkbookDescriptorWrapper>()
    init {
        val editContributorOption = MenuItem().apply {
            graphic = Label(messages["modifyContributors"]).apply {
                this.graphic = FontIcon(MaterialDesign.MDI_ACCOUNT_MULTIPLE)
                tooltip(this.text)
            }
            action {
                FX.eventbus.fire(ProjectContributorsEvent(books.map { it.workbookDescriptor }))
            }
        }
        val deleteOption = MenuItem().apply {
            addClass("danger")
            graphic = Label(messages["deleteProject"]).apply {
                this.graphic = FontIcon(MaterialDesign.MDI_DELETE)
                tooltip(this.text)
            }
            action {
                FX.eventbus.fire(ProjectGroupDeleteEvent(books.map { it.workbookDescriptor }))
            }
            disableWhen {
                books.booleanBinding { list -> list.any { it.progress > 0 } }
            }
        }
        addClass("wa-context-menu")
        isAutoHide = true
        items.setAll(editContributorOption, deleteOption)
    }
}