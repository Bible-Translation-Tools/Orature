package org.wycliffeassociates.otter.common.data.narration

import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import java.io.File
import kotlin.collections.ArrayList

interface NarrationAction {
    fun execute()
    fun undo()
    fun redo()
}

class NextVerseAction(
    private val list: ArrayList<VerseNode>,
    private val file: File
) : NarrationAction {
    private var node: VerseNode? = null

    override fun execute() {
        val start = list.lastOrNull()?.end ?: 0
        val end = file.length().toInt()

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

class RerecordAction(
    private val list: ArrayList<VerseNode>,
    private val file: File,
    private val verseIndex: Int
) : NarrationAction {
    var node: VerseNode? = null
    var previous: VerseNode? = null

    // Called when stop() is called after a re-record began
    override fun execute() {
        previous = list[verseIndex]

        val start = list.lastOrNull()?.end ?: 0
        val end = file.length().toInt()

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

class MarkerAction(
    private val list: ArrayList<VerseNode>,
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