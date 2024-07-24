/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.binding.Bindings
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.SourceTextDrawer
import org.wycliffeassociates.otter.jvm.controls.event.ChunkSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.ChunkingStepSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.GoToNextChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.GoToPreviousChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigateChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.BlindDraft
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.Chunking
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.ChunkingStepsDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.Consume
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.PeerEdit
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.ChapterReview
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.SourceAudioMissing
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.TranslationHeader
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.translationHeader
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ChunkingTranslationPage : View() {

    val viewModel: TranslationViewModel2 by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    private val mainFragmentProperty = viewModel.selectedStepProperty.objectBinding { step ->
        val fragment = when(step) {
            ChunkingStep.CONSUME_AND_VERBALIZE -> {
                if (viewModel.noSourceAudioProperty.value) {
                    find<SourceAudioMissing>()
                } else {
                    find<Consume>()
                }
            }
            ChunkingStep.CHUNKING -> find<Chunking>()
            ChunkingStep.BLIND_DRAFT -> find<BlindDraft>()
            ChunkingStep.PEER_EDIT,
            ChunkingStep.KEYWORD_CHECK,
            ChunkingStep.VERSE_CHECK -> find<PeerEdit>()
            ChunkingStep.FINAL_REVIEW -> find<ChapterReview>()
            null -> find<SourceAudioMissing>()
        }
        fragment.root
    }

    private lateinit var sourceTextDrawer: SourceTextDrawer
    private lateinit var header: TranslationHeader

    override val root = vbox {
        vgrow = Priority.ALWAYS

        translationHeader {
            header = this
            titleProperty.bind(viewModel.bookTitleProperty)
            chapterTitleProperty.bind(workbookDataStore.activeChapterTitleBinding())
            canUndoProperty.bind(viewModel.canUndoProperty)
            canRedoProperty.bind(viewModel.canRedoProperty)
            canGoNextProperty.bind(viewModel.isLastChapterProperty.not())
            canGoPreviousProperty.bind(viewModel.isFirstChapterProperty.not())
            canOpenInProperty.bind(viewModel.selectedStepProperty.booleanBinding { it == ChunkingStep.FINAL_REVIEW })
            Bindings.bindContent(chapterList, viewModel.chapterList)
        }

        borderpane {
            addClass("translation-view")
            vgrow = Priority.ALWAYS

            left = ChunkingStepsDrawer(viewModel.selectedStepProperty).apply {
                chunksProperty.bind(viewModel.chunkListProperty)
                reachableStepProperty.bind(viewModel.reachableStepProperty)
                noSourceAudioProperty.bind(viewModel.noSourceAudioProperty)
            }

            centerProperty().bind(mainFragmentProperty)

            right = SourceTextDrawer().apply {
                visibleWhen {
                    viewModel.selectedStepProperty.booleanBinding { step ->
                        step?.let {
                            val showSourceText = viewModel.noSourceAudioProperty.value ||
                                    it.ordinal >= ChunkingStep.PEER_EDIT.ordinal
                            showSourceText
                        } ?: false
                    }
                }
                managedWhen(visibleProperty())
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
        tryImportStylesheet("/css/chapter-grid.css")
        tryImportStylesheet("/css/translation-page.css")
        tryImportStylesheet("/css/consume-page.css")
        tryImportStylesheet("/css/blind-draft-page.css")
        tryImportStylesheet("/css/audio-player.css")
        tryImportStylesheet("/css/source-content.css")
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/marker-node.css")
        tryImportStylesheet("/css/scrolling-waveform.css")
        tryImportStylesheet("/css/source-audio-missing.css")
        tryImportStylesheet("/css/add-plugin-dialog.css")

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
        subscribe<NavigateChapterEvent> {
            header.dismissChapterSelector()
            viewModel.navigateChapter(it.chapterNumber)
        }
    }

    override fun onDock() {
        super.onDock()
        when (viewModel.pluginOpenedProperty.value) {
            true -> {
                // no-op, returning from plugin
            }
            false -> {
                viewModel.dockPage()
            }
        }
    }

    override fun onUndock() {
        super.onUndock()
        when (viewModel.pluginOpenedProperty.value) {
            true -> {
                // no-op, opening plugin
            }
            false -> {
                viewModel.undockPage()
            }
        }
    }
}