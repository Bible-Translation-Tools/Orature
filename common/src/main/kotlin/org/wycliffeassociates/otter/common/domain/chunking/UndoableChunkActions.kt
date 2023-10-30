package org.wycliffeassociates.otter.common.domain.chunking

import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.domain.IUndoable

abstract class ChunkTakeAction(
    protected val chunk: Chunk,
    protected val take: Take
) : IUndoable

class ChunkTakeRecordAction(
    chunk: Chunk,
    take: Take,
    private val previouslySelectedTake: Take? = null
) : ChunkTakeAction(chunk, take) {

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
    chunk: Chunk,
    take: Take,
    private val isTakeSelected: Boolean,
    private val postDeleteCallback: (Take, Boolean) -> Unit
) : ChunkTakeAction(chunk, take) {

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
    chunk: Chunk,
    take: Take,
    private val previouslySelectedTake: Take? = null
) : ChunkTakeAction(chunk, take) {
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

class ChunkTakeConfirmAction(
    private val take: Take,
    private val checking: CheckingStatus,
    private val oldCheckingStage: TakeCheckingState
) : IUndoable {
    override fun execute() {
        take.checkingState.accept(TakeCheckingState(checking, take.checksum()))
    }

    override fun undo() {
        take.checkingState.accept(oldCheckingStage)
    }

    override fun redo() = execute()
}