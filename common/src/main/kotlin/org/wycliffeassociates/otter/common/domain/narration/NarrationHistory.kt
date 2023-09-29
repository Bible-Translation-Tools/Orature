package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.audio.AudioFile
import java.util.*

internal class NarrationHistory {
    private val undoStack = ArrayDeque<NarrationAction>()
    private val redoStack = ArrayDeque<NarrationAction>()

    fun execute(
        action: NarrationAction,
        totalVerses: MutableList<VerseNode>,
        workingAudio: AudioFile
    ) {
        action.execute(totalVerses, workingAudio)
        undoStack.addLast(action)
        redoStack.clear()
    }

    fun undo(totalVerses: MutableList<VerseNode>) {
        if (undoStack.isEmpty()) return

        val action = undoStack.removeLast()

        action.undo(totalVerses)
        redoStack.addLast(action)
    }

    fun redo(totalVerses: MutableList<VerseNode>) {
        if (redoStack.isEmpty()) return

        val action = redoStack.removeLast()

        action.redo(totalVerses)
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

    fun finalizeVerse(end: Int, totalVerses: MutableList<VerseNode>) {
        when (val lastAction = undoStack.last) {
            is NewVerseAction -> lastAction.finalize(end, totalVerses)
            is RecordAgainAction -> lastAction.finalize(end, totalVerses)
            else -> {}
        }
    }
}