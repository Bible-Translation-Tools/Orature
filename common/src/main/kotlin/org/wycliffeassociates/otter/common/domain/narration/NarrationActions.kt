package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.wycliffeassociates.otter.common.audio.AudioFile
import kotlin.collections.ArrayList

/**
 * To perform a narration action,
 * and provide undo/redo functionality
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
internal interface NarrationAction {
    fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile)
    fun undo(activeVerses: MutableList<VerseNode>)
    fun redo(activeVerses: MutableList<VerseNode>)
}

/**
 * This action is to create a new verse node and add it to the list of verse nodes.
 * It doesn't track the end position of the verse. It should be updated when recording is paused.
 */
internal class NewVerseAction : NarrationAction {
    private var node: VerseNode? = null

    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        val start = workingAudio.totalFrames
        val end = workingAudio.totalFrames

        node = VerseNode (start, end).also {
            activeVerses.add(it)
        }
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        activeVerses.removeLast()
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        node?.let(activeVerses::add)
    }
}

/**
 * This action is to replace corresponding verse node in the list of verse nodes with new recording.
 * It doesn't track the end position of the verse. It should be updated when recording is stopped.
 */
internal class RecordAgainAction(
    private val verseIndex: Int
) : NarrationAction {
    var node: VerseNode? = null
    var previous: VerseNode? = null

    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        previous = activeVerses[verseIndex]

        val start = workingAudio.totalFrames
        val end = workingAudio.totalFrames

        node = VerseNode (start, end).also {
            activeVerses[verseIndex] = it
        }
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        previous?.let {
            activeVerses[verseIndex] = it
        }
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        node?.let {
            activeVerses[verseIndex] = it
        }
    }
}

/**
 * This action is to update verse location based on delta.
 * The previous verse's end position will also be updated.
 */
internal class VerseMarkerAction(
    private val verseIndex: Int,
    private val delta: Int
) : NarrationAction {
    private var oldNode: VerseNode? = null
    private var oldPrevNode: VerseNode? = null

    private var node: VerseNode? = null
    private var prevNode: VerseNode? = null

    // Called when marker is set and mouse button is released
    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        oldNode = activeVerses[verseIndex]
        oldPrevNode = activeVerses.getOrNull(verseIndex - 1)

        oldNode?.let { prev ->
            val newPos = prev.start + delta
            node = VerseNode(newPos, prev.end).also { current ->
                activeVerses[verseIndex] = current
            }
        }

        oldPrevNode?.let { prev ->
            val newPos = prev.end + delta
            prevNode = VerseNode(prev.start, newPos).also { current ->
                activeVerses[verseIndex - 1] = current
            }
        }
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        oldNode?.let {
            activeVerses[verseIndex] = it
        }
        oldPrevNode?.let {
            activeVerses[verseIndex - 1] = it
        }
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        node?.let {
            activeVerses[verseIndex] = it
        }
        prevNode?.let {
            activeVerses[verseIndex - 1] = it
        }
    }
}

/**
 * This action is to replace corresponding verse node in the list of verse nodes
 * with new recording from an external app.
 */
internal class EditVerseAction(
    private val verseIndex: Int,
    private val start: Int,
    private val end: Int
): NarrationAction {
    var node: VerseNode? = null
    var previous: VerseNode? = null

    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        previous = activeVerses[verseIndex]

        node = VerseNode (start, end).also {
            activeVerses[verseIndex] = it
        }
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        previous?.let {
            activeVerses[verseIndex] = it
        }
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        node?.let {
            activeVerses[verseIndex] = it
        }
    }
}

/**
 * This action is to clear the list of verse nodes
 */
internal class ResetAllAction: NarrationAction {
    private val nodes = ArrayList<VerseNode>()

    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        nodes.addAll(activeVerses)
        activeVerses.clear()
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        activeVerses.addAll(nodes)
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        activeVerses.clear()
    }
}

internal class ChapterEditedAction(
    private val newList: List<VerseNode>
): NarrationAction {
    private val nodes = ArrayList<VerseNode>()

    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        nodes.addAll(activeVerses)
        activeVerses.clear()
        activeVerses.addAll(newList)
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        activeVerses.clear()
        activeVerses.addAll(nodes)
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        activeVerses.clear()
        activeVerses.addAll(newList)
    }
}