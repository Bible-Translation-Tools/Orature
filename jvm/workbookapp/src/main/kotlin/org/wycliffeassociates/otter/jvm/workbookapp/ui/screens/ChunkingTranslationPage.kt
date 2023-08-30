package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.SourceTextDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.ChunkingStepsDrawer
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
    private val sourceTextProperty = SimpleStringProperty(null)
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
            vgrow = Priority.ALWAYS

            left = ChunkingStepsDrawer().apply {
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

            right = SourceTextDrawer().apply {
                textProperty.bind(sourceTextProperty)
            }
        }
    }

    init {
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunking-page.css")
        tryImportStylesheet("/css/source-content.css")
    }

    override fun onDock() {
        super.onDock()
        sourceTextProperty.set("1. Source text here.\n2. More source text here...")
    }

    override fun onUndock() {
        super.onUndock()
    }
}

class ChunkingFragment : Fragment() {
    override val root = VBox().apply {
        label("this is chunking").addClass("h3")
    }
}

class BlindDraftFragment : Fragment() {
    override val root = VBox().apply {
        label("this is blind draft").addClass("h3")
    }
}