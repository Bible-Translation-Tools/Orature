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
package org.wycliffeassociates.otter.jvm.controls

import javafx.animation.TranslateTransition
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.util.Duration

class ListAnimationMediator<T: Node> {

    var listView: ListView<T>? = null
    var node: T? = null
    var isAnimating = false
        private set

    fun animate(callback: () -> Unit) {
        listView?.let {
            node?.let { _node ->
                isAnimating = true
                shiftOtherNodes()

                val parentY = _node.parent.layoutY
                _node.styleClass.add("selected")

                // move selected node to top of the list
                val ttUp = TranslateTransition(Duration.millis(600.0), _node)
                ttUp.toY = -parentY
                ttUp.onFinished = EventHandler {
                    revertAnimation(_node) {
                        _node.styleClass.remove("selected")
                        isAnimating = false
                        callback()
                    }
                }
                ttUp.play()
            }
        }
    }

    private fun shiftOtherNodes() {
        listView?.let { _listView ->
            node?.let { _node ->
                val selectedIndex = _listView.items.indexOf(_node)
                for (item in _listView.items) {
                    if (_listView.items.indexOf(item) < selectedIndex) {
                        moveDown(item)
                    }
                }
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
