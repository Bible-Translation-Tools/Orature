package org.wycliffeassociates.otter.jvm.controls

import javafx.animation.TranslateTransition
import javafx.scene.Node
import javafx.util.Duration
import tornadofx.*
import kotlin.math.abs

class TakeSelectionAnimationMediator<T: Node> {
    var node: T? = null
    var itemList = observableListOf<Node>()
    var selectedNode: T? = null

    var isAnimating = false
        private set

    fun animate(onFinishCallback: () -> Unit) {
        if (node == null || itemList.isEmpty() || selectedNode == null) {
            onFinishCallback()
            return
        }
        isAnimating = true
        val node = node!!
        val selectedNode = selectedNode!!

        val upTranslation = TranslateTransition(Duration.millis(500.0), node).apply {
            byY = -verticalDistance(node, selectedNode)
            setOnFinished {
                node.revertAnimation {
                    isAnimating = false
                    onFinishCallback()
                }
            }
        }
        upTranslation.play()
        selectedNode.moveDown(verticalDistance(selectedNode, itemList.first()))
        shiftOtherNodes(node)
    }

    private fun shiftOtherNodes(animatedNode: T) {
        val indexThreshold = itemList.indexOf(animatedNode)
        itemList.forEachIndexed { index, node ->
            if (index < indexThreshold) {
                val distance = abs(itemList[index + 1].boundsInParent.minY - itemList[index].boundsInParent.minY)
                node.moveDown(distance)
            }
        }
    }

    private fun Node.moveDown(distance: Double) {
        val tt = TranslateTransition(Duration.millis(600.0), this)
        tt.byY = distance
        tt.setOnFinished {
            revertAnimation()
        }
        tt.play()
    }

    private fun Node.revertAnimation(onFinishCallback: () -> Unit = { }) {
        val distance = translateY
        val ttRevertY = TranslateTransition(Duration.millis(1.0), node).apply {
            byY = -distance
            setOnFinished {
                onFinishCallback()
            }
        }
        ttRevertY.play()
    }

    private fun verticalDistance(node1: Node, node2: Node): Double {
        val minY1 = node1.localToScene(node1.boundsInLocal).minY
        val minY2 = node2.localToScene(node2.boundsInLocal).minY

        return abs(minY1 - minY2)
    }
}