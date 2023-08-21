package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import kotlin.collections.ArrayList

/**
 * To perform a narration action,
 * and provide undo/redo functionality
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
internal interface NarrationAction {
    fun execute(totalVerses: MutableList<VerseNode>, workingAudio: AudioFile)
    fun undo(totalVerses: MutableList<VerseNode>)
    fun redo(totalVerses: MutableList<VerseNode>)
}

/**
 * This action is to create a new verse node and add it to the list of verse nodes.
 * It doesn't track the end position of the verse. It should be updated when recording is paused.
 */
internal class NewVerseAction(
    private val verseIndex: Int
) : NarrationAction {
    private val logger = LoggerFactory.getLogger(NarrationAction::class.java)

    internal var node: VerseNode? = null

    override fun execute(
        totalVerses: MutableList<VerseNode>, workingAudio: AudioFile
    ) {
        logger.info("New verse for index: ${verseIndex}")

        val start = workingAudio.totalFrames
        val end = workingAudio.totalFrames

        node = VerseNode(
            start, end, placed = true, totalVerses[verseIndex].marker.copy()
        ).also {
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing verse for index: ${verseIndex}")
        totalVerses[verseIndex].placed = false
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        logger.info("Redoing verse for index: ${verseIndex}")
        node?.let {
            totalVerses[verseIndex] = it.copy()
        }
    }
}

/**
 * This action is to replace corresponding verse node in the list of verse nodes with new recording.
 * It doesn't track the end position of the verse. It should be updated when recording is stopped.
 */
internal class RecordAgainAction(
    private val verseIndex: Int
) : NarrationAction {
    private val logger = LoggerFactory.getLogger(RecordAgainAction::class.java)

    internal var node: VerseNode? = null
    internal var previous: VerseNode? = null

    override fun execute(
        totalVerses: MutableList<VerseNode>, workingAudio: AudioFile
    ) {
        logger.info("Recording again verse for index: ${verseIndex}")
        previous = totalVerses[verseIndex]

        val start = workingAudio.totalFrames
        val end = workingAudio.totalFrames

        node = VerseNode(
            start, end, placed = true, totalVerses[verseIndex].marker.copy()
        ).also {
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing record again for index: ${verseIndex}")
        previous?.let {
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        logger.info("Redoing record again for index: ${verseIndex}")
        node?.let {
            totalVerses[verseIndex] = it.copy()
        }
    }
}

/**
 * This action is to replace corresponding verse nodes in the list of verse nodes
 * by verse nodes with updated positions.
 */
internal class VerseMarkerAction(
    private val verseIndex: Int, private val newMarkerPosition: Int
) : NarrationAction {
    private val logger = LoggerFactory.getLogger(VerseMarkerAction::class.java)

    private var previousFirstNode: VerseNode? = null
    private var previousSecondNode: VerseNode? = null

    private var firstNode: VerseNode? = null
    private var secondNode: VerseNode? = null

    // Called when marker is set and mouse button is released
    override fun execute(
        totalVerses: MutableList<VerseNode>, workingAudio: AudioFile
    ) {
        logger.info("Moving marker of verse index: ${verseIndex}")
        previousFirstNode = totalVerses[verseIndex]
        previousSecondNode = totalVerses.getOrNull(verseIndex - 1)

        previousFirstNode?.let { prev ->

            val start = newMarkerPosition
            val end = prev.end

            firstNode = VerseNode(
                start, end, placed = true, totalVerses[verseIndex].marker.copy()
            ).also { current ->
                totalVerses[verseIndex] = current.copy()
            }
        }

        previousSecondNode?.let { prev ->

            val start = prev.start
            val end = newMarkerPosition

            secondNode = VerseNode(
                start, end, placed = true, totalVerses[verseIndex - 1].marker.copy()
            ).also { current ->
                totalVerses[verseIndex - 1] = current.copy()
            }
        }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing moving marker of verse index: ${verseIndex}")
        previousFirstNode?.let {
            totalVerses[verseIndex] = it.copy()
        }
        previousSecondNode?.let {
            totalVerses[verseIndex - 1] = it.copy()
        }
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing moving marker of verse index: ${verseIndex}")
        firstNode?.let {
            totalVerses[verseIndex] = it.copy()
        }
        secondNode?.let {
            totalVerses[verseIndex - 1] = it.copy()
        }
    }
}

/**
 * This action is to replace corresponding verse node in the list of verse nodes
 * with new recording from an external app.
 */
internal class EditVerseAction(
    private val verseIndex: Int, private val start: Int, private val end: Int
) : NarrationAction {
    private val logger = LoggerFactory.getLogger(EditVerseAction::class.java)

    var node: VerseNode? = null
    var previous: VerseNode? = null

    override fun execute(totalVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        logger.info("Editing verse index: ${verseIndex}")
        previous = totalVerses[verseIndex]

        val vm = totalVerses[verseIndex].marker.copy()
        node = VerseNode(
            start, end, placed = true, vm
        ).also {
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing edit verse index: ${verseIndex}")
        previous?.let {
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        logger.info("Redoing edit verse index: ${verseIndex}")
        node?.let {
            totalVerses[verseIndex] = it.copy()
        }
    }
}

/**
 * This action is to clear the list of verse nodes
 */
internal class ResetAllAction : NarrationAction {
    private val nodes = ArrayList<VerseNode>()

    override fun execute(totalVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        // use copy to get nodes that won't share the same pointer otherwise clearing totalVerses will result in
        // erasing the state from nodes as well.
        nodes.addAll(totalVerses.map { it.copy() })
        totalVerses.forEach { it.clear() }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        totalVerses.clear()
        // same as with execute; copy the nodes otherwise undo/redo will start erasing the data saved in nodes.
        totalVerses.addAll(nodes.map { it.copy() })
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        totalVerses.forEach { it.clear() }
    }
}

internal class ChapterEditedAction(
    private val newList: List<VerseNode>
) : NarrationAction {
    private val nodes = ArrayList<VerseNode>()

    override fun execute(totalVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        nodes.addAll(totalVerses)
        totalVerses.clear()
        totalVerses.addAll(newList)
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        totalVerses.clear()
        totalVerses.addAll(nodes)
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        totalVerses.clear()
        totalVerses.addAll(newList)
    }
}