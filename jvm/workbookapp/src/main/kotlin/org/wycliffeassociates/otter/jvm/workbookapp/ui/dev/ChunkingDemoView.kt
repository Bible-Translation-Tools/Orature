package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.Node
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChunkingStepNode
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.chunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid.ChunkGrid
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.*

class ChunkingDemoView : View() {

    private val selectedChunk: IntegerProperty = SimpleIntegerProperty(-1)
    private val currentStepProperty = SimpleObjectProperty<ChunkingStep>()

    private val list = listOf(
        ChunkViewData(1, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), selectedChunk)
    )

    override val root = vbox {
        maxWidth = 320.0

        val grid = ChunkGrid(list)
        chunkingStep(ChunkingStep.CONSUME, currentStepProperty, null)
        chunkingStep(ChunkingStep.CHUNKING, currentStepProperty, null)
        chunkingStep(ChunkingStep.BLIND_DRAFT, currentStepProperty, grid)
        chunkingStep(ChunkingStep.PEER_EDIT, currentStepProperty, grid)
        chunkingStep(ChunkingStep.KEYWORD_CHECK, currentStepProperty, grid)
        chunkingStep(ChunkingStep.VERSE_CHECK, currentStepProperty, grid)
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
    }
}