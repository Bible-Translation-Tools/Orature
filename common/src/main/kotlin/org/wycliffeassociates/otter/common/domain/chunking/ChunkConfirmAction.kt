package org.wycliffeassociates.otter.common.domain.chunking

import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.domain.IUndoable

class ChunkConfirmAction(
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