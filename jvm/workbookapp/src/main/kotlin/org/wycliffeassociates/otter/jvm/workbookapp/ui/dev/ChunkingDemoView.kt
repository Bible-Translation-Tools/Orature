package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Parent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.chunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid.ChunkGrid
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.ChunkingStepsPane
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.chunkingStepsPane
import tornadofx.*

class ChunkingDemoView : View() {
    private val fragments = mapOf(
        ChunkingStep.CONSUME_AND_VERBALIZE to ConsumeFragment(),
        ChunkingStep.CHUNKING to ChunkingFragment(),
        ChunkingStep.BLIND_DRAFT to BlindDraftFragment()
    )

    private val selectedChunk: IntegerProperty = SimpleIntegerProperty(2)
    private val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)
    private val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.PEER_EDIT)
    private val list = observableListOf(
        ChunkViewData(1, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), selectedChunk)
    )

    override val root = vbox {
        borderpane {
            left = ChunkingStepsPane().apply {
                chunkItems.setAll(list)
                this.reachableStepProperty.bind(this@ChunkingDemoView.reachableStepProperty)
                this@ChunkingDemoView.selectedStepProperty.bind(this.selectedStepProperty)
            }
            centerProperty().bind(selectedStepProperty.objectBinding {
                it?.let {
                    fragments[it]?.root
                }
            })
        }

    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
    }
}

class ConsumeFragment : Fragment() {
    override val root = VBox().apply {
        button("this is consume")
    }
}

class ChunkingFragment : Fragment() {
    override val root = VBox().apply {
        button("this is chunking")
    }
}

class BlindDraftFragment : Fragment() {
    override val root = VBox().apply {
        button("this is blind draft")
    }
}