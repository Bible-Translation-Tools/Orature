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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.animation.TranslateTransition
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.util.Duration
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel

class TakeCell(
    private val onTakeSelected: (TakeModel) -> Unit
) : ListCell<TakeModel>() {
    private val view = TakeItem()

    override fun updateItem(item: TakeModel?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }
        graphic = view.apply {
            selectedProperty.set(item.selected)
            takeProperty.set(item)

            setOnTakeSelected {
                if (isAnimatingProperty.value || item.selected) {
                    return@setOnTakeSelected
                }
                isAnimatingProperty.set(true)
                animate(item) {
                    onTakeSelected(item)
                }
            }
        }
    }

    private fun animate(takeCard: TakeModel, callback: () -> Unit) {
        shiftOtherNodes(takeCard)

        val parentY = view.parent.layoutY
        view.styleClass.add("selected")

        // move selected node to top of the list
        val ttUp = TranslateTransition(Duration.millis(600.0), view)
        ttUp.toY = -parentY
        ttUp.onFinished = EventHandler {
            view.styleClass.remove("selected")
            revertAnimation(view) { view.isAnimatingProperty.set(false) }
            callback()
        }
        ttUp.play()
    }

    private fun shiftOtherNodes(takeCard: TakeModel) {
        val selectedIndex = listView.items.indexOf(takeCard)
        for (item in listView.items) {
            if (listView.items.indexOf(item) < selectedIndex) {
                moveDown(view)
            }
        }
    }

    private fun moveDown(node: Node) {
        val distance = node.boundsInLocal.height + 5
        val tt = TranslateTransition(Duration.millis(600.0), node)
        tt.byY = distance
        tt.onFinished = EventHandler {
            revertAnimation(node)
        }
        tt.play()
    }

    private fun revertAnimation(node: Node, onFinish: () -> Unit = { }) {
        val distance = node.translateY
        val ttRevertY = TranslateTransition(Duration.millis(1.0), node)
        ttRevertY.byY = -distance
        ttRevertY.onFinished = EventHandler {
            onFinish()
        }
        ttRevertY.play()
    }
}
