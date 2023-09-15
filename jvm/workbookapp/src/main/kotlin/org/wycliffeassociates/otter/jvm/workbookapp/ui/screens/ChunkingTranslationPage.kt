package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.SourceTextDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.ChunkSelectedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.ChunkingStepSelectedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.BlindDraft
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.Chunking
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.ChunkingStepsDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.Consume
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ChunkingTranslationPage : View() {

    val viewModel: TranslationViewModel2 by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    private val mainFragmentProperty = viewModel.selectedStepProperty.objectBinding {
        it?.let { step ->
            when(step) {
                ChunkingStep.CONSUME_AND_VERBALIZE -> Consume()
                ChunkingStep.CHUNKING -> Chunking()
                ChunkingStep.BLIND_DRAFT -> BlindDraft()
                else -> null
            }
        }
    }

    private lateinit var sourceTextDrawer: SourceTextDrawer

    override val root = vbox {
        vgrow = Priority.ALWAYS

        borderpane {
            vgrow = Priority.ALWAYS

            left = ChunkingStepsDrawer(viewModel.selectedStepProperty).apply {
                chunksProperty.bind(viewModel.chunkListProperty)
                this.reachableStepProperty.bind(viewModel.reachableStepProperty)
            }

            centerProperty().bind(mainFragmentProperty.objectBinding { it?.root })

            right = SourceTextDrawer().apply {
                sourceTextDrawer = this
                textProperty.bind(viewModel.sourceTextProperty)
                highlightedChunk.bind(viewModel.currentMarkerProperty)
            }
        }
    }

    init {
        tryImportStylesheet("/css/consume-page.css")
        tryImportStylesheet("/css/chunking-page.css")
        tryImportStylesheet("/css/blind-draft-page.css")
        tryImportStylesheet("/css/source-content.css")
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunk-marker.css")
        tryImportStylesheet("/css/scrolling-waveform.css")

        mainFragmentProperty.addListener { observable, oldValue, newValue ->
            oldValue?.onUndock()
            newValue?.onDock()
        }

        subscribe<ChunkingStepSelectedEvent> {
            viewModel.navigateStep(it.step)
        }
        subscribe<ChunkSelectedEvent> {
            viewModel.selectChunk(it.chunkNumber)
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.dockPage()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undockPage()
    }
}