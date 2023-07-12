package org.wycliffeassociates.otter.common.data.narration

import com.fasterxml.jackson.annotation.JsonAutoDetect
import java.util.*

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
internal class NarrationHistory {
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

    fun hasUndo(): Boolean {
        return undoStack.isNotEmpty()
    }

    fun hasRedo(): Boolean {
        return redoStack.isNotEmpty()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}