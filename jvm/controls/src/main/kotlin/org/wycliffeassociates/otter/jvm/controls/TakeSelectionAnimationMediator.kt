package org.wycliffeassociates.otter.jvm.controls

import javafx.animation.TranslateTransition
import javafx.scene.Node
import javafx.util.Duration
import tornadofx.*
import kotlin.math.abs

class TakeSelectionAnimationMediator<T: Node> {
    var node: T? = null
    var nodeList = observableListOf<Node>()
    var selectedNode: T? = null

    var isAnimating = false
        private set

    fun animate(onFinishCallback: () -> Unit) {
        if (node == null || nodeList.isEmpty() || selectedNode == null) {
            onFinishCallback()
            return
        }
        isAnimating = true
        val node = node!!
        val selectedNode = selectedNode!!

        node.moveUp(-verticalDistance(node, selectedNode), onFinishCallback)
        selectedNode.moveDown(verticalDistance(selectedNode, nodeList.first()))
        shiftOtherNodes(node)
    }

    private fun shiftOtherNodes(animatedNode: T) {
        val indexThreshold = nodeList.indexOf(animatedNode)
        nodeList.forEachIndexed { index, node ->
            if (index < indexThreshold) {
                val distance = abs(nodeList[index + 1].boundsInParent.minY - nodeList[index].boundsInParent.minY)
                node.moveDown(distance)
            }
        }
    }

    private fun Node.moveUp(distance: Double, callback: () -> Unit = {}) {
        val up = TranslateTransition(Duration.millis(500.0), this)
        up.byY = distance
        up.setOnFinished {
            revertAnimation {
                isAnimating = false
                callback()
            }
        }
        up.play()
    }

    private fun Node.moveDown(distance: Double) {
        val down = TranslateTransition(Duration.millis(500.0), this)
        down.byY = distance
        down.setOnFinished {
            revertAnimation()
        }
        down.play()
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