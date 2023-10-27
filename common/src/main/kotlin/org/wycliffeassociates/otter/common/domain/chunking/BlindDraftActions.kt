package org.wycliffeassociates.otter.common.domain.chunking

import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.IUndoable

abstract class ChunkTakeAction(
    protected val take: Take,
    protected val chunk: Chunk
) : IUndoable

class ChunkTakeRecordAction(
    take: Take,
    chunk: Chunk,
    private val previouslySelectedTake: Take? = null
) : ChunkTakeAction(take, chunk) {

    override fun execute() {
        chunk.audio.insertTake(take)
    }

    override fun undo() {
        take.deletedTimestamp.accept(DateHolder.now())
        previouslySelectedTake?.let {
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
    take: Take,
    chunk: Chunk,
    private val isTakeSelected: Boolean,
    private val postDeleteCallback: (Take, Boolean) -> Unit
) : ChunkTakeAction(take, chunk) {

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
    take: Take,
    chunk: Chunk,
    private val previouslySelectedTake: Take? = null
) : ChunkTakeAction(take, chunk) {
    override fun execute() {
        take.file.setLastModified(System.currentTimeMillis())
        chunk.audio.selectTake(take)
    }

    override fun undo() {
        previouslySelectedTake?.let {
            chunk.audio.selectTake(it)
        }
    }

    override fun redo() = execute()
}