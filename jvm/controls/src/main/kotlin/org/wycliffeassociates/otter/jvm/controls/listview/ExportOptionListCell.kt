/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.listview

import javafx.scene.control.Button
import javafx.scene.control.ListCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportOption
import tornadofx.FX.Companion.messages
import tornadofx.addClass
import tornadofx.get

class ExportOptionListCell : ListCell<ExportOption>() {

    private fun associatedIcon(option: ExportOption): FontIcon = when (option) {
        ExportOption.BACKUP -> FontIcon(MaterialDesign.MDI_FOLDER_MULTIPLE_OUTLINE)
        ExportOption.LISTEN -> FontIcon(MaterialDesign.MDI_PLAY)
        ExportOption.SOURCE_AUDIO -> FontIcon(Material.HEARING)
        ExportOption.PUBLISH -> FontIcon(Material.CLOUD_UPLOAD)
    }

    override fun updateItem(item: ExportOption?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            return
        }

        graphic = Button().apply {
            addClass(
                "btn", "btn--tertiary", "btn--borderless", "export-menu__option-btn"
            )
            text = messages[item.titleKey]
            isMouseTransparent = true
            graphic = associatedIcon(item)
        }
    }
}

class DummyExportComboBoxButton : ListCell<String>() {

    override fun updateItem(item: String?, btl: Boolean) {
        super.updateItem(item, btl)
        if (item != null || !btl) {
            graphic = Button(item).apply {
                addClass(
                    "btn", "btn--tertiary", "btn--borderless", "dummy-export-menu__btn"
                )
                graphic = FontIcon(MaterialDesign.MDI_FILE_EXPORT)
            }
        }
    }
}