package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import com.sun.javafx.scene.ParentHelper
import com.sun.javafx.scene.traversal.Algorithm
import com.sun.javafx.scene.traversal.Direction
import com.sun.javafx.scene.traversal.ParentTraversalEngine
import com.sun.javafx.scene.traversal.TraversalContext
import javafx.scene.Node
import javafx.scene.Parent

class DrawerTraversalEngine(root: Parent) {
    private var prevNode: Node? = null
    private var prevDirection: Direction? = null

    private val algorithm = object: Algorithm {
        override fun select(owner: Node, dir: Direction, context: TraversalContext): Node {
            return traverse(owner, dir, context)
        }

        private fun traverse(node: Node, dir: Direction, context: TraversalContext): Node {
            prevDirection = prevDirection ?: dir
            var index = context.allTargetNodes.indexOf(prevNode)

            when (dir) {
                Direction.DOWN, Direction.RIGHT, Direction.NEXT, Direction.NEXT_IN_LINE -> {
                    when (prevDirection) {
                        Direction.DOWN, Direction.RIGHT, Direction.NEXT, Direction.NEXT_IN_LINE -> index++
                    }
                }
                Direction.LEFT, Direction.PREVIOUS, Direction.UP -> {
                    when (prevDirection) {
                        Direction.LEFT, Direction.PREVIOUS, Direction.UP -> index--
                    }
                }
            }

            prevNode = node
            prevDirection = dir

            if (index < 0) {
                index = context.allTargetNodes.lastIndex
            }
            index %= context.allTargetNodes.size

            return context.allTargetNodes[index]
        }

        override fun selectFirst(context: TraversalContext): Node {
            return context.allTargetNodes.first()
        }

        override fun selectLast(context: TraversalContext): Node {
            return context.allTargetNodes.last()
        }
    }

    init {
        val engine = ParentTraversalEngine(root, algorithm)
        ParentHelper.setTraversalEngine(root, engine)
    }

    fun reset() {
        prevNode = null
        prevDirection = null
    }
}
