package org.wycliffeassociates.otter.common.data.narration

import java.util.*

class NarrationHistory {
    private val undoStack = ArrayDeque<NarrationAction>()
    private val redoStack = ArrayDeque<NarrationAction>()

    fun execute(action: NarrationAction) {
        action.execute()
        undoStack.addLast(action)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isEmpty()) return

        val action = undoStack.removeLast()

        action.undo()
        redoStack.addLast(action)
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        val action = redoStack.removeLast()

        action.redo()
        undoStack.addLast(action)
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}