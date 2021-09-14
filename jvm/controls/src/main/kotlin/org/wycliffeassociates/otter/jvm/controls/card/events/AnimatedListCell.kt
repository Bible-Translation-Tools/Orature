package org.wycliffeassociates.otter.jvm.controls.card.events

import javafx.animation.TranslateTransition
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.util.Duration

abstract class AnimatedListCell<T> : ListCell<T>() {
    abstract val view: Node

    fun animate(takeModel: T, callback: () -> Unit) {
        shiftOtherNodes(takeModel)

        val parentY = view.parent.layoutY

        // move selected node to top of the list
        val ttUp = TranslateTransition(Duration.millis(600.0), view)
        ttUp.toY = -parentY
        ttUp.onFinished = EventHandler {
            revertAnimation(view) {
                callback()
            }
        }
        ttUp.play()
    }

    private fun shiftOtherNodes(takeModel: T) {
        val selectedIndex = listView.items.indexOf(takeModel)
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
