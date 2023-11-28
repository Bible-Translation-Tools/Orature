package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.binding.Bindings
import javafx.scene.Node
import javafx.scene.control.Separator
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.SourceTextDrawer
import org.wycliffeassociates.otter.jvm.controls.event.ChunkSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.ChunkingStepSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.event.GoToNextChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.GoToPreviousChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.OpenChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.BlindDraft
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.Chunking
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.ChunkingStepsDrawer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.Consume
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.PeerEdit
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.ChapterReview
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation.translationHeader
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class ChunkingTranslationPage : View() {

    val viewModel: TranslationViewModel2 by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    private val sourceAudioMissingView = createSourceAudioMissingView()

    private val mainFragmentProperty = viewModel.selectedStepProperty.objectBinding {
        it?.let { step ->
            val fragment = when(step) {
                ChunkingStep.CONSUME_AND_VERBALIZE -> find<Consume>()
                ChunkingStep.CHUNKING -> find<Chunking>()
                ChunkingStep.BLIND_DRAFT -> find<BlindDraft>()
                ChunkingStep.PEER_EDIT,
                ChunkingStep.KEYWORD_CHECK,
                ChunkingStep.VERSE_CHECK -> find<PeerEdit>()
                ChunkingStep.FINAL_REVIEW -> find<ChapterReview>()
            }
            fragment.root
        } ?: sourceAudioMissingView
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
            Bindings.bindContent(chapterList, viewModel.chapterList)
        }

        borderpane {
            vgrow = Priority.ALWAYS

            left = ChunkingStepsDrawer(viewModel.selectedStepProperty).apply {
                chunksProperty.bind(viewModel.chunkListProperty)
                this.reachableStepProperty.bind(viewModel.reachableStepProperty)
            }

            centerProperty().bind(mainFragmentProperty)

            right = SourceTextDrawer().apply {
                visibleWhen {
                    viewModel.selectedStepProperty.booleanBinding { step ->
                        step?.let {
                            it.ordinal >= ChunkingStep.PEER_EDIT.ordinal
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
        tryImportStylesheet("/css/consume-page.css")
        tryImportStylesheet("/css/chunking-page.css")
        tryImportStylesheet("/css/blind-draft-page.css")
        tryImportStylesheet("/css/audio-player.css")
        tryImportStylesheet("/css/source-content.css")
        tryImportStylesheet("/css/chunk-item.css")
        tryImportStylesheet("/css/marker-node.css")
        tryImportStylesheet("/css/scrolling-waveform.css")
        tryImportStylesheet("/css/source-audio-missing.css")

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
        subscribe<OpenChapterEvent> {
            viewModel.navigateChapter(it.chapterNumber)
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

    private fun createSourceAudioMissingView(): Node {
        return VBox().apply {
            addClass("audio-missing-view")
            vgrow = Priority.ALWAYS

            label {
                textProperty().bind(
                    workbookDataStore.activeChapterTitleBinding().stringBinding {
                        MessageFormat.format(
                            messages["source_audio_missing_for"],
                            it
                        )
                    }
                )
            }
            label(messages["source_audio_missing_description"]) {
                addClass("normal-text")
            }

            vbox {
                addClass("drag-drop-area")

                label {
                    addClass("")
                    graphic = FontIcon(MaterialDesign.MDI_FOLDER_OUTLINE)
                }

                label(messages["dragToImport"]) {
                    fitToParentWidth()
                    addClass("")
                }

                button(messages["browseFiles"]) {
                    addClass(
                        "btn",
                        "btn--primary"
                    )
                    tooltip(text)
                    graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                    action {
                        chooseFile(
                            FX.messages["importResourceFromZip"],
                            arrayOf(
                                FileChooser.ExtensionFilter(
                                    messages["oratureFileTypes"],
                                    *OratureFileFormat.extensionList.map { "*.$it" }.toTypedArray()
                                )
                            ),
                            mode = FileChooserMode.Single,
                            owner = currentWindow
                        )//.firstOrNull()?.let { importFile(it) }
                    }
                }
            }

            stackpane {
                addClass("separator-area")
                separator { fitToParentWidth() }
            }

            label(messages["need_source_audio"]) {
                addClass("h3")
            }

            textflow {
                text(messages["source_audio_download_description"]) {
                    addClass("normal-text")
                }
                hyperlink("audio.bibleineverylanguage.org").apply {
                    addClass("wa-text--hyperlink", "app-drawer__text--link")
                    tooltip("audio.bibleineverylanguage.org/gl")
                    action {
                        hostServices.showDocument("https://audio.bibleineverylanguage.org/gl")
                    }
                }
            }

            hbox {
                button(messages["check_online"]) {
                    addClass("btn", "btn--primary")
                    tooltip(text)
                    graphic = FontIcon(MaterialDesign.MDI_EXPORT)
                }
                button(messages["begin_narrating_book_chapter"]) {
                    addClass("btn", "btn--secondary")
                    graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                }
            }
        }
    }
}