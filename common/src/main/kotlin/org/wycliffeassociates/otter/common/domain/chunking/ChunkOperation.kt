package org.wycliffeassociates.otter.common.domain.chunking

import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState

interface ChunkOperation {
    fun apply()
    fun undo()
    fun redo()
}

class ChunkConfirmAction(
    private val take: Take,
    private val checking: CheckingStatus,
    private val oldCheckingStage: TakeCheckingState
) : ChunkOperation {
    override fun apply() {
        take.checkingState.accept(TakeCheckingState(checking, take.checksum()))
    }

    override fun undo() {
        take.checkingState.accept(oldCheckingStage)
    }

    override fun redo() = apply()
}