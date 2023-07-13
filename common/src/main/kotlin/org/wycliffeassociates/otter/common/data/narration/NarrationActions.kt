package org.wycliffeassociates.otter.common.data.narration

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import kotlin.collections.ArrayList

/**
 * To perform a narration action,
 * and provide undo/redo functionality
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
interface NarrationAction {
    fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile)
    fun undo(activeVerses: MutableList<VerseNode>)
    fun redo(activeVerses: MutableList<VerseNode>)
}

/**
 * This action is to create a new verse node and add it to the list of verse nodes.
 * It doesn't track the end position of the verse. It should be updated when recording is paused.
 */
class NewVerseAction : NarrationAction {
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
class RecordAgainAction(
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
 * This action is to replace corresponding verse nodes in the list of verse nodes
 * by verse nodes with updated positions.
 */
class VerseMarkerAction(
    private val firstVerseIndex: Int,
    private val secondVerseIndex: Int,
    private val marker: Int
) : NarrationAction {
    private var previousFirstNode: VerseNode? = null
    private var previousSecondNode: VerseNode? = null

    private var firstNode: VerseNode? = null
    private var secondNode: VerseNode? = null

    // Called when marker is set and mouse button is released
    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        previousFirstNode = activeVerses[firstVerseIndex]
        previousSecondNode = activeVerses[secondVerseIndex]

        previousFirstNode?.let { prev ->
            firstNode = VerseNode(prev.start, marker).also { current ->
                activeVerses[firstVerseIndex] = current
            }
        }

        previousSecondNode?.let { prev ->
            secondNode = VerseNode(marker, prev.end).also { current ->
                activeVerses[secondVerseIndex] = current
            }
        }
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        previousFirstNode?.let {
            activeVerses[firstVerseIndex] = it
        }
        previousSecondNode?.let {
            activeVerses[secondVerseIndex] = it
        }
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        firstNode?.let {
            activeVerses[firstVerseIndex] = it
        }
        secondNode?.let {
            activeVerses[secondVerseIndex] = it
        }
    }
}

/**
 * This action is to replace corresponding verse node in the list of verse nodes
 * with new recording from an external app.
 */
class EditVerseAction(
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
class ResetAllAction: NarrationAction {
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

class ChapterEditedAction(
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