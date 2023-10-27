package org.wycliffeassociates.otter.common.domain.chunking

import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.IUndoable

class ChunkTakeRecordAction(
    private val take: Take,
    private val chunk: Chunk,
    private val oldSelectedTake: Take? = null
) : IUndoable {

    override fun execute() {
        chunk.audio.insertTake(take)
    }

    override fun undo() {
        take.deletedTimestamp.accept(DateHolder.now())
        oldSelectedTake?.let {
            chunk.audio.selectTake(it)
        }
    }

    override fun redo() {
        chunk.audio
            .getAllTakes()
            .find { it == take }
            ?.deletedTimestamp
            ?.accept(DateHolder.empty)

        take.file.setLastModified(System.currentTimeMillis())
        chunk.audio.selectTake(take)
    }
}

class ChunkTakeDeleteAction(
    private val take: Take,
    private val chunk: Chunk,
    private val isTakeSelected: Boolean,
    private val postDeleteCallback: (Take, Boolean) -> Unit
) : IUndoable {

    override fun execute() {
        take.deletedTimestamp.accept(DateHolder.now())
        postDeleteCallback(take, isTakeSelected)
    }

    override fun undo() {
        take.file.setLastModified(System.currentTimeMillis())
        chunk.audio.getAllTakes()
            .find { it == take }?.deletedTimestamp?.accept(DateHolder.empty)

        if (isTakeSelected) {
            chunk.audio.selectTake(take)
        }
    }

    override fun redo() = execute()
}

class ChunkTakeSelectAction(
    private val take: Take,
    private val chunk: Chunk,
    private val oldSelectedTake: Take? = null
) : IUndoable {
    override fun execute() {
        take.file.setLastModified(System.currentTimeMillis())
        chunk.audio.selectTake(take)
    }

    override fun undo() {
        oldSelectedTake?.let {
            chunk.audio.selectTake(it)
        }
    }

    override fun redo() = execute()
}