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
package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model

import com.sun.javafx.collections.ObservableListWrapper
import javafx.collections.ListChangeListener

class ResourceGroupCardItemList : ObservableListWrapper<ResourceGroupCardItem>(mutableListOf()) {
    init {
        addListener(
            ListChangeListener<ResourceGroupCardItem> {
                while (it.next()) {
                    if (it.wasRemoved()) {
                        it.removed.forEach { item ->
                            item.onRemove()
                        }
                    }
                }
            }
        )
    }
}
