package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import java.util.*

internal class NarrationHistory {
    private val undoStack = ArrayDeque<NarrationAction>()
    private val redoStack = ArrayDeque<NarrationAction>()

    fun execute(
        action: NarrationAction,
        activeVerses: MutableList<VerseNode>,
        workingAudio: AudioFile
    ) {
        action.execute(activeVerses, workingAudio)
        undoStack.addLast(action)
        redoStack.clear()
    }

    fun undo(activeVerses: MutableList<VerseNode>) {
        if (undoStack.isEmpty()) return

        val action = undoStack.removeLast()

        action.undo(activeVerses)
        redoStack.addLast(action)
    }

    fun redo(activeVerses: MutableList<VerseNode>) {
        if (redoStack.isEmpty()) return

        val action = redoStack.removeLast()

        action.redo(activeVerses)
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