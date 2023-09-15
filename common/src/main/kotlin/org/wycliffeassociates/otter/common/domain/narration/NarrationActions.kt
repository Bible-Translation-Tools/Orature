package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

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

        val start = if (workingAudio.totalFrames == 0) 0 else workingAudio.totalFrames + 1
        val end = start

        node = VerseNode(
            start, end, placed = true, totalVerses[verseIndex].marker.copy()
        ).also {
            it.addStart(start)
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

    fun finalize(end: Int, totalVerses: MutableList<VerseNode>) {
        node?.let { node ->
            node.finalize(end)
            totalVerses[verseIndex] = node.copy()
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
        previous = totalVerses[verseIndex].copy()

        val start = if (workingAudio.totalFrames == 0) 0 else workingAudio.totalFrames + 1
        val end = start

        node = VerseNode(
            start,
            end,
            placed = true,
            totalVerses[verseIndex].marker.copy(),
            mutableListOf(start..end)
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

    fun finalize(end: Int, totalVerses: MutableList<VerseNode>) {
        node?.let { node ->
            node.finalize(end)
            totalVerses[verseIndex] = node.copy()
        }
    }
}

/**
 * This action is to replace corresponding verse nodes in the list of verse nodes
 * by verse nodes with updated positions.
 *
 * The Verse Index corresponds to the marker that was moved, meaning that the marker separates the verse corresponding
 * to the verse index and the previous verse.
 */
internal class MoveMarkerAction(
    private val verseIndex: Int, private val delta: Int
) : NarrationAction {
    private val logger = LoggerFactory.getLogger(MoveMarkerAction::class.java)

    private var oldPrecedingVerse: VerseNode? = null
    private var oldVerse: VerseNode? = null

    private var precedingVerse: VerseNode? = null
    private var verse: VerseNode? = null

    // Called when marker is set and mouse button is released
    override fun execute(
        totalVerses: MutableList<VerseNode>, workingAudio: AudioFile
    ) {
        logger.info("Moving marker of verse index: ${verseIndex}")
        oldPrecedingVerse = totalVerses.getOrNull(verseIndex - 1)?.copy()
        oldVerse = totalVerses[verseIndex].copy()

        val bothNull = oldPrecedingVerse == null && oldVerse == null
        val markerMovedBetweenVerses = oldPrecedingVerse != null && oldVerse != null
        val firstMarkerMoved = oldPrecedingVerse == null && oldVerse != null

        when {
            bothNull -> {
                throw IllegalStateException("Verse markers not found beginning at $verseIndex")
            }

            firstMarkerMoved -> {
                logger.warn("First marker moved, no other markers placed?")
                verse = oldVerse!!.copy()
                if (delta < 0) {
                    verse!!.takeFramesFromStart(delta.absoluteValue)
                } else {
                    verse!!.addRange(listOf(delta.absoluteValue..verse!!.firstFrame()))
                }

                totalVerses[verseIndex] = verse!!.copy()
            }

            markerMovedBetweenVerses -> {
                precedingVerse = oldPrecedingVerse!!.copy()
                verse = oldVerse!!.copy()

                if (delta < 0) {
                    val framesToAdd = precedingVerse!!.takeFramesFromEnd(delta.absoluteValue)
                    verse!!.sectors.addAll(0, framesToAdd)
                } else {
                    val framesToAdd = verse!!.takeFramesFromStart(delta.absoluteValue)
                    precedingVerse!!.sectors.addAll(framesToAdd)
                }

                totalVerses[verseIndex] = verse!!.copy()
                totalVerses[verseIndex - 1] = precedingVerse!!.copy()
            }
        }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing moving marker of verse index: ${verseIndex}")
        oldVerse?.let {
            totalVerses[verseIndex] = it.copy()
        }
        oldPrecedingVerse?.let {
            totalVerses[verseIndex - 1] = it.copy()
        }
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing moving marker of verse index: ${verseIndex}")
        verse?.let {
            totalVerses[verseIndex] = it.copy()
        }
        precedingVerse?.let {
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