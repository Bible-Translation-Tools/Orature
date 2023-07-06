package org.wycliffeassociates.otter.common.data.narration

import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import kotlin.collections.ArrayList

/**
 * To perform a narration action,
 * and provide undo/redo functionality
 */
interface NarrationAction {
    fun execute()
    fun undo()
    fun redo()
}

/**
 * This action is to create a new verse node and add it to the list of verse nodes.
 * It doesn't track the end position of the verse. It should be updated when recording is paused.
 */
class NextVerseAction(
    private val list: MutableList<VerseNode>,
    private val file: AudioFile
) : NarrationAction {
    private var node: VerseNode? = null

    override fun execute() {
        val start = file.totalFrames
        val end = file.totalFrames

        node = VerseNode (start, end).also {
            list.add(it)
        }
    }

    override fun undo() {
        list.removeLast()
    }

    override fun redo() {
        node?.let(list::add)
    }
}

/**
 * This action is to replace corresponding verse node in the list of verse nodes with new recording.
 * It doesn't track the end position of the verse. It should be updated when recording is stopped.
 */
class RecordAgainAction(
    private val list: MutableList<VerseNode>,
    private val file: AudioFile,
    private val verseIndex: Int
) : NarrationAction {
    var node: VerseNode? = null
    var previous: VerseNode? = null

    override fun execute() {
        previous = list[verseIndex]

        val start = file.totalFrames
        val end = file.totalFrames

        node = VerseNode (start, end).also {
            list[verseIndex] = it
        }
    }

    override fun undo() {
        previous?.let {
            list[verseIndex] = it
        }
    }

    override fun redo() {
        node?.let {
            list[verseIndex] = it
        }
    }
}

/**
 * This action is to replace corresponding verse nodes in the list of verse nodes
 * by verse nodes with updated positions.
 */
class VerseMarkerAction(
    private val list: MutableList<VerseNode>,
    private val firstVerseIndex: Int,
    private val secondVerseIndex: Int,
    private val marker: Int
) : NarrationAction {
    private var previousFirstNode: VerseNode? = null
    private var previousSecondNode: VerseNode? = null

    private var firstNode: VerseNode? = null
    private var secondNode: VerseNode? = null

    // Called when marker is set and mouse button is released
    override fun execute() {
        previousFirstNode = list[firstVerseIndex]
        previousSecondNode = list[secondVerseIndex]

        previousFirstNode?.let { prev ->
            firstNode = VerseNode(prev.start, marker).also { current ->
                list[firstVerseIndex] = current
            }
        }

        previousSecondNode?.let { prev ->
            secondNode = VerseNode(marker, prev.end).also { current ->
                list[secondVerseIndex] = current
            }
        }
    }

    override fun undo() {
        previousFirstNode?.let {
            list[firstVerseIndex] = it
        }
        previousSecondNode?.let {
            list[secondVerseIndex] = it
        }
    }

    override fun redo() {
        firstNode?.let {
            list[firstVerseIndex] = it
        }
        secondNode?.let {
            list[secondVerseIndex] = it
        }
    }
}

/**
 * This action is to replace corresponding verse node in the list of verse nodes
 * with new recording from an external app.
 */
class EditVerseAction(
    private val list: MutableList<VerseNode>,
    private val verseIndex: Int,
    private val start: Int,
    private val end: Int
): NarrationAction {
    var node: VerseNode? = null
    var previous: VerseNode? = null

    override fun execute() {
        previous = list[verseIndex]

        node = VerseNode (start, end).also {
            list[verseIndex] = it
        }
    }

    override fun undo() {
        previous?.let {
            list[verseIndex] = it
        }
    }

    override fun redo() {
        node?.let {
            list[verseIndex] = it
        }
    }

}

/**
 * This action is to clear the list of verse nodes
 */
class ResetAllAction(private val list: MutableList<VerseNode>): NarrationAction {
    private val nodes = ArrayList<VerseNode>(list.size)

    override fun execute() {
        nodes.addAll(list)
        list.clear()
    }

    override fun undo() {
        list.addAll(nodes)
    }

    override fun redo() {
        list.clear()
    }

}

class ChapterEditedAction(
    private val list: MutableList<VerseNode>,
    private val newList: List<VerseNode>
): NarrationAction {
    private val nodes = ArrayList<VerseNode>(list.size)

    override fun execute() {
        nodes.addAll(list)
        list.clear()
        list.addAll(newList)
    }

    override fun undo() {
        list.clear()
        list.addAll(nodes)
    }

    override fun redo() {
        list.clear()
        list.addAll(newList)
    }

}