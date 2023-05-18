package org.wycliffeassociates.otter.common.data.narration

import java.util.*

class NarrationHistory {
    private val undoStack = Stack<NarrationAction>()
    private val redoStack = Stack<NarrationAction>()

    fun execute(action: NarrationAction) {
        action.execute()
        undoStack.push(action)
        redoStack.clear()
    }

    fun undo() {
        val action = undoStack.pop()
        if (action != null) {
            action.undo()
            redoStack.push(action)
        }
    }

    fun redo() {
        val action = redoStack.pop()
        if (action != null) {
            action.redo()
            undoStack.push(action)
        }
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}