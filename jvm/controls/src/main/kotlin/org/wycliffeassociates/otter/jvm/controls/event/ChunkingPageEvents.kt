package org.wycliffeassociates.otter.jvm.controls.event

import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import tornadofx.FXEvent

class ChunkingStepSelectedEvent(val step: ChunkingStep) : FXEvent()
class ChunkSelectedEvent(val chunkNumber: Int): FXEvent()
class ChunkTakeEvent(val take: Take, val action: TakeAction): FXEvent()
enum class TakeAction {
    SELECT,
    EDIT,
    DELETE
}
class MarkerDeletedEvent(val markerId: Int): FXEvent()
class MarkerMovedEvent(val markerId: Int, val start: Int, val end: Int): FXEvent()
class UndoChunkingPageEvent: FXEvent()
class RedoChunkingPageEvent: FXEvent()