/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
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
        logger.info("New marker added: ${totalVerses[verseIndex].marker.formattedLabel}")

        val start = if (workingAudio.totalFrames == 0) 0 else workingAudio.totalFrames + 1
        val end = start

        node = VerseNode(
            placed = true, totalVerses[verseIndex].marker.clone()
        ).also {
            it.addStart(start)
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing new marker: ${totalVerses[verseIndex].marker.formattedLabel}")
        totalVerses[verseIndex].placed = false
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        node?.let {
            logger.info("Redoing new marker: ${totalVerses[verseIndex].marker.formattedLabel}")
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
        logger.info("Recording again for: ${totalVerses[verseIndex].marker.formattedLabel}")
        previous = totalVerses[verseIndex].copy()

        val start = if (workingAudio.totalFrames == 0) 0 else workingAudio.totalFrames + 1
        val end = start

        node = VerseNode(
            placed = true,
            totalVerses[verseIndex].marker.clone(),
            mutableListOf(start..end)
        ).also {
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        previous?.let {
            logger.info("Undoing record again for: ${totalVerses[verseIndex].marker.formattedLabel}")
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        node?.let {
            logger.info("Redoing record again for: ${totalVerses[verseIndex].marker.formattedLabel}")
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
        logger.info("Moving marker: ${totalVerses[verseIndex].marker.formattedLabel} by ${delta} frames")
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
        oldVerse?.let {
            logger.info("Undoing moving marker: ${totalVerses[verseIndex].marker.formattedLabel}")
            totalVerses[verseIndex] = it.copy()
        }
        oldPrecedingVerse?.let {
            totalVerses[verseIndex - 1] = it.copy()
        }
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        verse?.let {
            logger.info("Redoing moving marker: ${totalVerses[verseIndex].marker.formattedLabel}")
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
        logger.info("Editing: ${totalVerses[verseIndex].marker.formattedLabel}")
        previous = totalVerses[verseIndex]

        val vm = totalVerses[verseIndex].marker.clone()
        node = VerseNode(
            placed = true,
            vm,
            mutableListOf(start..end)
        ).also {
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        previous?.let {
            logger.info("Undoing edit: ${totalVerses[verseIndex].marker.formattedLabel}")
            totalVerses[verseIndex] = it.copy()
        }
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        node?.let {
            logger.info("Redoing edit: ${totalVerses[verseIndex].marker.formattedLabel}")
            totalVerses[verseIndex] = it.copy()
        }
    }
}

/**
 * This action is to clear the list of verse nodes
 */
internal class ResetAllAction(private val chapterAudio: AssociatedAudio) : NarrationAction {
    private val logger = LoggerFactory.getLogger(ResetAllAction::class.java)
    private val nodes = ArrayList<VerseNode>()
    private var recoverableTake: Take? = null

    override fun execute(totalVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        logger.info("Reset all action: clearing all markers")
        // use copy to get nodes that won't share the same pointer otherwise clearing totalVerses will result in
        // erasing the state from nodes as well.
        nodes.addAll(totalVerses.map { it.copy() })
        totalVerses.forEach { it.clear() }
        chapterAudio
            .getSelectedTake()
            ?.also { recoverableTake = it }
            ?.deletedTimestamp
            ?.accept(DateHolder.now())
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing reset all action")

        totalVerses.clear()
        // same as with execute; copy the nodes otherwise undo/redo will start erasing the data saved in nodes.
        totalVerses.addAll(nodes.map { it.copy() })
        recoverableTake?.let {
            it.deletedTimestamp
                .accept(DateHolder.empty)
                .also {
                    chapterAudio.selectTake(recoverableTake)
                }
        }
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        logger.info("Redoing reset all action")

        totalVerses.forEach { it.clear() }
        chapterAudio
            .getSelectedTake()
            ?.deletedTimestamp
            ?.accept(DateHolder.now())
    }
}

internal class ChapterEditedAction(
    private val newList: List<VerseNode>
) : NarrationAction {
    private val logger = LoggerFactory.getLogger(ChapterEditedAction::class.java)

    private val nodes = ArrayList<VerseNode>()

    override fun execute(totalVerses: MutableList<VerseNode>, workingAudio: AudioFile) {
        logger.info("Chapter edited action")

        nodes.addAll(totalVerses)
        totalVerses.clear()
        totalVerses.addAll(newList)
    }

    override fun undo(totalVerses: MutableList<VerseNode>) {
        logger.info("Undoing chapter edited action")

        totalVerses.clear()
        totalVerses.addAll(nodes)
    }

    override fun redo(totalVerses: MutableList<VerseNode>) {
        logger.info("Redoing chapter edited action")

        totalVerses.clear()
        totalVerses.addAll(newList)
    }
}