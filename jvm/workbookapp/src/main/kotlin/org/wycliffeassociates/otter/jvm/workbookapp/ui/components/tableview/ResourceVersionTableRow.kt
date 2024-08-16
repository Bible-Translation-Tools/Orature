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

import javafx.scene.control.TableRow
import org.wycliffeassociates.otter.jvm.controls.event.ResourceVersionSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.model.ResourceVersion
import tornadofx.*

class ResourceVersionTableRow : TableRow<ResourceVersion>() {
    override fun updateItem(item: ResourceVersion?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            isMouseTransparent = true
            return
        }

        isMouseTransparent = isDisable
        isFocusTraversable = false

        setOnMouseClicked {
            if (it.clickCount == 1) { // avoid double fire()
                FX.eventbus.fire(ResourceVersionSelectedEvent(item))
            }
        }
    }
}