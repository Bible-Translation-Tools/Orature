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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import tornadofx.*

class TakesListView(
    items: ObservableList<TakeCardModel>,
    createTakeNode: (TakeCardModel) -> Node
) : ListView<TakeCardModel>(items) {
    init {
        cellFormat {
            /* Don't use cell caching, because we remove the front node of the take card when it is dragged
                and we don't ever add it back if it was made the selected take. (This is because we create a
                new take card if it was selected.)
             */
            if (!it.selected) {
                graphic = createTakeNode(it)
            }
        }
        vgrow = Priority.ALWAYS
        isFocusTraversable = false
        addClass("card__takes-list")

        childrenUnmodifiable.onChange { removeListViewClip(this as ListView<Any>) }
    }
}

/**
 * The built-in clipped container from the ListView
 * overlays the timestamp tooltip of the audio player.
 * This function extends the clipped region towards the
 * top boundary, allowing the tooltip to be fully visible.
 *
 * Call this method after the list view children have rendered.
 */
fun removeListViewClip(lv: ListView<Any>) {
    // traverse to ClippedContainer and update it
    lv.getChildList()?.firstOrNull { it.hasClass("virtual-flow") }
        ?.getChildList()?.firstOrNull { it.hasClass("clipped-container") }
        ?.clip = null
}
