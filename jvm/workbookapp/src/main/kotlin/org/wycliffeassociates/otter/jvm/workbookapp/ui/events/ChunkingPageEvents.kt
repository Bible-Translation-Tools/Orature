package org.wycliffeassociates.otter.jvm.workbookapp.ui.events

import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.FXEvent

class ChunkingStepSelectedEvent(val step: ChunkingStep) : FXEvent()
