/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view

import javafx.beans.property.BooleanProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceGroupCardItem
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.styles.ResourceListStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import tornadofx.*

class ResourceListView(
    items: ObservableList<ResourceGroupCardItem>,
    filterCompletedCardsProperty: BooleanProperty,
    navigator: NavigationMediator
) : ListView<ResourceGroupCardItem>(items) {
    init {
        cellFormat {
            graphic = cache(it.title) {
                resourcegroupcard(it, filterCompletedCardsProperty, navigator)
            }
        }

        vgrow = Priority.ALWAYS
        isFocusTraversable = false
        addClass(ResourceListStyles.resourceGroupList)
    }
}
