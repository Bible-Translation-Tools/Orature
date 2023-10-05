package org.wycliffeassociates.otter.jvm.workbookapp.ui.events

import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.FXEvent

class ChunkingStepSelectedEvent(val step: ChunkingStep) : FXEvent()
class ChunkSelectedEvent(val chunkNumber: Int): FXEvent()
class ChunkTakeEvent(val take: Take, val action: TakeAction): FXEvent()
enum class TakeAction {
    SELECT,
    EDIT,
    DELETE
}
