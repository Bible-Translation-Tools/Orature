package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.SourceTextDrawer
import org.wycliffeassociates.otter.jvm.controls.event.ChunkSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.ChunkingStepSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.GoToNextChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.GoToPreviousChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.BlindDraft
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.Chunking
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.ChunkingStepsDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.Consume
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.KeywordCheck
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.PeerEdit
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.ChapterReview
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.VerseCheck
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking.translationHeader
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ChunkingTranslationPage : View() {

    val viewModel: TranslationViewModel2 by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    private val mainFragmentProperty = viewModel.selectedStepProperty.objectBinding {
        it?.let { step ->
            when(step) {
                ChunkingStep.CONSUME_AND_VERBALIZE -> find<Consume>()
                ChunkingStep.CHUNKING -> find<Chunking>()
                ChunkingStep.BLIND_DRAFT -> find<BlindDraft>()
                ChunkingStep.PEER_EDIT -> find<PeerEdit>()
                ChunkingStep.KEYWORD_CHECK -> find<KeywordCheck>()
                ChunkingStep.VERSE_CHECK -> find<VerseCheck>()
                ChunkingStep.CHAPTER_REVIEW -> find<ChapterReview>()
            }
        }
    }

    private lateinit var sourceTextDrawer: SourceTextDrawer

    override val root = vbox {
        vgrow = Priority.ALWAYS

        translationHeader {
            titleProperty.bind(
                workbookDataStore.activeWorkbookProperty.stringBinding {
                    it?.target?.title
                }
            )
            chapterTitleProperty.bind(workbookDataStore.activeChapterTitleBinding())
            canUndoProperty.bind(viewModel.canUndoProperty)
            canRedoProperty.bind(viewModel.canRedoProperty)
            canGoNextProperty.bind(viewModel.isLastChapterProperty.not())
            canGoPreviousProperty.bind(viewModel.isFirstChapterProperty.not())
        }

        borderpane {
            vgrow = Priority.ALWAYS

            left = ChunkingStepsDrawer(viewModel.selectedStepProperty).apply {
                chunksProperty.bind(viewModel.chunkListProperty)
                this.reachableStepProperty.bind(viewModel.reachableStepProperty)
            }

            centerProperty().bind(mainFragmentProperty.objectBinding { it?.root })

            right = SourceTextDrawer().apply {
                sourceTextDrawer = this
                sourceInfoProperty.bind(viewModel.sourceInfoProperty)
                licenseProperty.bind(viewModel.sourceLicenseProperty)
                textProperty.bind(viewModel.sourceTextProperty)
                highlightedChunk.bind(viewModel.currentMarkerProperty)
            }
        }
    }

    init {
        tryImportStylesheet("/css/chapter-selector.css")
        tryImportStylesheet("/css/consume-page.css")
        tryImportStylesheet("/css/chunking-page.css")
        tryImportStylesheet("/css/blind-draft-page.css")
        tryImportStylesheet("/css/audio-player.css")
        tryImportStylesheet("/css/source-content.css")
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/marker-node.css")
        tryImportStylesheet("/css/scrolling-waveform.css")

        subscribe<ChunkingStepSelectedEvent> {
            viewModel.navigateStep(it.step)
        }
        subscribe<ChunkSelectedEvent> {
            viewModel.selectChunk(it.chunkNumber)
        }
        subscribe<GoToNextChapterEvent> {
            viewModel.nextChapter()
        }
        subscribe<GoToPreviousChapterEvent> {
            viewModel.previousChapter()
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