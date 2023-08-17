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

        val verseIndex = activeVerses.indexOfFirst { !it.placed }

        verseIndex?.let {
            node = VerseNode(
                start,
                end,
                placed = true,
                activeVerses[verseIndex].marker.copy()
            ).also {
                activeVerses[verseIndex] = it
            }
        }
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        val index = activeVerses.indexOfLast { it.placed }
        index?.let { activeVerses[index].placed = false }
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        val index = activeVerses.indexOfFirst { !it.placed }
        index?.let { activeVerses[index].placed = true }
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

    override fun execute(
        activeVerses: MutableList<VerseNode>,
        workingAudio: AudioFile
    ) {
        previous = activeVerses[verseIndex]

        val start = workingAudio.totalFrames
        val end = workingAudio.totalFrames

        node = VerseNode(
            start,
            end,
            placed = true,
            activeVerses[verseIndex].marker.copy()
        ).also {
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
internal class VerseMarkerAction(
    private val verseIndex: Int,
    private val newMarkerPosition: Int
) : NarrationAction {
    private var previousFirstNode: VerseNode? = null
    private var previousSecondNode: VerseNode? = null

    private var firstNode: VerseNode? = null
    private var secondNode: VerseNode? = null

    // Called when marker is set and mouse button is released
    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        previousFirstNode = activeVerses[verseIndex]
        previousSecondNode = activeVerses.getOrNull(verseIndex - 1)

        previousFirstNode?.let { prev ->
            firstNode = VerseNode(
                newMarkerPosition,
                prev.end,
                placed = true,
                activeVerses[verseIndex].marker.copy()
            ).also { current ->
                activeVerses[verseIndex] = current
            }
        }

        previousSecondNode?.let { prev ->
            secondNode = VerseNode(
                prev.start,
                newMarkerPosition,
                placed = true,
                activeVerses[verseIndex - 1].marker.copy()
            ).also { current ->
                activeVerses[verseIndex - 1] = current
            }
        }
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        previousFirstNode?.let {
            activeVerses[verseIndex] = it
        }
        previousSecondNode?.let {
            activeVerses[verseIndex - 1] = it
        }
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        firstNode?.let {
            activeVerses[verseIndex] = it
        }
        secondNode?.let {
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
) : NarrationAction {
    var node: VerseNode? = null
    var previous: VerseNode? = null

    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        previous = activeVerses[verseIndex]

        val vm = activeVerses[verseIndex].marker.copy()
        node = VerseNode(
            start,
            end,
            placed = true,
            vm
        ).also {
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
internal class ResetAllAction : NarrationAction {
    private val nodes = ArrayList<VerseNode>()

    override fun execute(activeVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        nodes.addAll(activeVerses)
        activeVerses.forEach { it.clear() }
    }

    override fun undo(activeVerses: MutableList<VerseNode>) {
        activeVerses.clear()
        activeVerses.addAll(nodes)
    }

    override fun redo(activeVerses: MutableList<VerseNode>) {
        activeVerses.forEach { it.clear() }
    }
}

internal class ChapterEditedAction(
    private val newList: List<VerseNode>
) : NarrationAction {
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