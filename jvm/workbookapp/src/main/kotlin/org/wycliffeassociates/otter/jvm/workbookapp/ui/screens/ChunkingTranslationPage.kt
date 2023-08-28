package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.ChunkingStepsPane
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.Consume
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import tornadofx.*

class ChunkingTranslationPage : View() {

    val viewModel: ChunkingViewModel by inject()

    private val fragments = mapOf(
        ChunkingStep.CONSUME_AND_VERBALIZE to Consume(),
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
        vgrow = Priority.ALWAYS

        borderpane {
            left = ChunkingStepsPane().apply {
                chunkItems.setAll(list)
                this.reachableStepProperty.bind(this@ChunkingTranslationPage.reachableStepProperty)
                this@ChunkingTranslationPage.selectedStepProperty.bind(this.selectedStepProperty)
            }
            centerProperty().bind(selectedStepProperty.objectBinding {
                it?.let {
                    fragments[it]?.onDock()
                    fragments[it]?.root
                }
            })
        }
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
    }

    override fun onDock() {
        super.onDock()
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