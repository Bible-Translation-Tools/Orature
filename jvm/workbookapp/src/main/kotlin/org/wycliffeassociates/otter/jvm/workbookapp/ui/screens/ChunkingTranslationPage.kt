package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.SourceTextDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.ChunkingStepSelectedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.ChunkingStepsDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.Consume
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ChunkingTranslationPage : View() {

    val viewModel: ChunkingViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    private val list = observableListOf(
        ChunkViewData(1, SimpleBooleanProperty(true), viewModel.selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), viewModel.selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), viewModel.selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), viewModel.selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), viewModel.selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), viewModel.selectedChunk)
    )
    private val mainFragmentProperty = viewModel.selectedStepProperty.objectBinding {
        it?.let { step ->
            when(step) {
                ChunkingStep.CONSUME_AND_VERBALIZE -> Consume()
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
                chunkItems.setAll(list)
                this.reachableStepProperty.bind(viewModel.reachableStepProperty)
            }

            centerProperty().bind(mainFragmentProperty.objectBinding { it?.root })

            right = SourceTextDrawer().apply {
                sourceTextDrawer = this
                textProperty.bind(viewModel.sourceTextProperty)
                highlightedChunk.bind(viewModel.currentMarkerNumberProperty)
            }
        }
    }

    init {
        tryImportStylesheet("/css/consume-page.css")
        tryImportStylesheet("/css/chunking-page.css")
        tryImportStylesheet("/css/source-content.css")
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/chunk-marker.css")
        tryImportStylesheet("/css/scrolling-waveform.css")

        mainFragmentProperty.addListener { observable, oldValue, newValue ->
            oldValue?.onUndock()
            newValue?.onDock()
        }

        subscribe<ChunkingStepSelectedEvent> {
            viewModel.selectedStepProperty.set(it.step)
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.dockPage()
        viewModel.selectedStepProperty.set(ChunkingStep.CONSUME_AND_VERBALIZE)
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.selectedStepProperty.set(null)
    }
}