package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.chunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid.ChunkGrid
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.*

class ChunkingDemoView : View() {

    private val selectedChunk: IntegerProperty = SimpleIntegerProperty(2)
    private val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)
    private val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.PEER_EDIT)
    private val isCollapsedProperty = SimpleBooleanProperty(false)
    private val list = observableListOf(
        ChunkViewData(1, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), selectedChunk)
    )

    override val root = vbox {
        maxWidth = 300.0
        maxWidth = 320.0

        val grid = ChunkGrid(list)
        scrollpane {
            isFitToWidth = true
            vbox {
                chunkingStep(ChunkingStep.CONSUME_AND_VERBALIZE,selectedStepProperty,reachableStepProperty, isCollapsedProperty, null)
                chunkingStep(ChunkingStep.CHUNKING, selectedStepProperty, reachableStepProperty, isCollapsedProperty, null)
                chunkingStep(ChunkingStep.BLIND_DRAFT, selectedStepProperty, reachableStepProperty, isCollapsedProperty, grid)
                chunkingStep(ChunkingStep.PEER_EDIT, selectedStepProperty, reachableStepProperty, isCollapsedProperty, grid)
                chunkingStep(ChunkingStep.KEYWORD_CHECK, selectedStepProperty, reachableStepProperty, isCollapsedProperty, grid)
                chunkingStep(ChunkingStep.VERSE_CHECK, selectedStepProperty, reachableStepProperty, isCollapsedProperty, grid)
            }
        }
        button("Collapse") {
            action {
                this@vbox.maxWidth = if (isCollapsedProperty.value) {
                    320.0
                } else {
                    80.0
                }
                isCollapsedProperty.set(!isCollapsedProperty.value)
            }
        }
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
    }
}

