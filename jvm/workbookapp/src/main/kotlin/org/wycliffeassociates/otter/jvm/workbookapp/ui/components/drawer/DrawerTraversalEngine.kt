package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.scene.ParentHelper
import com.sun.javafx.scene.traversal.Algorithm
import com.sun.javafx.scene.traversal.Direction
import com.sun.javafx.scene.traversal.ParentTraversalEngine
import com.sun.javafx.scene.traversal.TraversalContext
import javafx.scene.Node
import javafx.scene.Parent

class DrawerTraversalEngine(root: Parent) {
    private val algorithm = object: Algorithm {
        private val targetNodes = mutableListOf<Node>()

        override fun select(owner: Node, dir: Direction, context: TraversalContext): Node {
            return traverse(owner, dir, context)
        }

        private fun traverse(node: Node, dir: Direction, context: TraversalContext): Node {
            if (!isFocusable(node)) return node

            targetNodes.clear()
            addFocusableChildrenToTargetNodes(context.root)

            var index = targetNodes.indexOf(node)

            when (dir) {
                Direction.DOWN, Direction.RIGHT, Direction.NEXT, Direction.NEXT_IN_LINE -> index++
                Direction.LEFT, Direction.PREVIOUS, Direction.UP -> index--
            }

            if (index < 0) {
                index = targetNodes.lastIndex
            }

            if (targetNodes.size > 0) {
                index %= targetNodes.size
            }

            return targetNodes[index]
        }

        override fun selectFirst(context: TraversalContext): Node {
            return if (targetNodes.isNotEmpty()) targetNodes.first() else root
        }

        override fun selectLast(context: TraversalContext): Node {
            return if (targetNodes.isNotEmpty()) targetNodes.last() else root
        }

        private fun addFocusableChildrenToTargetNodes(parent: Parent) {
            val parentsNodes: List<Node> = parent.childrenUnmodifiable
            for (node in parentsNodes) {
                if (isFocusable(node)) {
                    targetNodes.add(node)
                }
                if (node is Parent) {
                    addFocusableChildrenToTargetNodes(node)
                }
            }
        }

        private fun isFocusable(node: Node): Boolean {
            return node.isFocusTraversable && NodeHelper.isTreeVisible(node) && !node.isDisabled
        }
    }

    init {
        val engine = ParentTraversalEngine(root, algorithm)
        ParentHelper.setTraversalEngine(root, engine)
    }
}
