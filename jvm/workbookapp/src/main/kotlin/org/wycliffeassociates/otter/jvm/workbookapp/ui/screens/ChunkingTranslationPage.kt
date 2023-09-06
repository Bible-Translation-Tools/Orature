package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.github.thomasnield.rxkotlinfx.observeOnFx
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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ChunkingTranslationPage : View() {

    val viewModel: ChunkingViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    private val fragments = mapOf(
        ChunkingStep.CONSUME_AND_VERBALIZE to find<Consume>(),
        ChunkingStep.CHUNKING to ChunkingFragment(),
        ChunkingStep.BLIND_DRAFT to BlindDraftFragment()
    )
    private val selectedChunk: IntegerProperty = SimpleIntegerProperty(2)
    private val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)
    private val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.PEER_EDIT)
    private val sourceTextProperty = SimpleStringProperty()
    private val list = observableListOf(
        ChunkViewData(1, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(2, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(3, SimpleBooleanProperty(true), selectedChunk),
        ChunkViewData(4, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(5, SimpleBooleanProperty(false), selectedChunk),
        ChunkViewData(6, SimpleBooleanProperty(false), selectedChunk)
    )
    private val mainFragmentProperty = selectedStepProperty.objectBinding {
        it?.let {
            fragments[it]
        }
    }

    private lateinit var sourceTextDrawer: SourceTextDrawer

    override val root = vbox {
        vgrow = Priority.ALWAYS

        borderpane {
            vgrow = Priority.ALWAYS

            left = ChunkingStepsDrawer(selectedStepProperty).apply {
                chunkItems.setAll(list)
                this.reachableStepProperty.bind(this@ChunkingTranslationPage.reachableStepProperty)
            }

            centerProperty().bind(mainFragmentProperty.objectBinding { it?.root })

            right = SourceTextDrawer().apply {
                sourceTextDrawer = this
                textProperty.bind(sourceTextProperty)
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
    }

    override fun onDock() {
        super.onDock()
        val recentChapter = workbookDataStore.workbookRecentChapterMap.getOrDefault(
            workbookDataStore.workbook.hashCode(),
            1
        )
        val chapter = workbookDataStore.workbook.target.chapters
            .filter { it.sort == recentChapter }
            .blockingFirst()

        workbookDataStore.activeChapterProperty.set(chapter)
        workbookDataStore.getSourceText()
            .observeOnFx()
            .subscribe {
                sourceTextProperty.set(it)
            }
    }

    override fun onUndock() {
        super.onUndock()
        selectedStepProperty.set(null)
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