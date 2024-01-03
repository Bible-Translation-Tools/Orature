package org.wycliffeassociates.otter.common.domain.model

import org.wycliffeassociates.otter.common.domain.IUndoable
import java.util.*
import java.util.ArrayDeque

class UndoableActionHistory<T : IUndoable> {
    private val undoStack: Deque<IUndoable> = ArrayDeque()
    private val redoStack: Deque<IUndoable> = ArrayDeque()

    fun execute(action: T) {
        undoStack.push(action)
        redoStack.clear()
        action.execute()
    }

    fun undo() {
        if (undoStack.isEmpty()) return

        val op = undoStack.pop()
        redoStack.push(op)
        op.undo()
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        val action = redoStack.pop()
        undoStack.push(action)
        action.redo()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    fun canUndo() = undoStack.isNotEmpty()

    fun canRedo() = redoStack.isNotEmpty()
}
