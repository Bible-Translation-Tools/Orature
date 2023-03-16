package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterSelector
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.narration.floatingnarrationcard
import org.wycliffeassociates.otter.jvm.controls.narration.narrationrecordlistview
import org.wycliffeassociates.otter.jvm.controls.narration.narrationtextlistview
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationRecordCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterNarrationViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class ChapterNarrationPage : View() {
    private val logger = LoggerFactory.getLogger(ChapterNarrationPage::class.java)

    private val viewModel: ChapterNarrationViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            workbookDataStore.activeChapterProperty.stringBinding {
                it?.let {
                    MessageFormat.format(
                        messages["chapterTitle"],
                        messages["chapter"],
                        it.sort
                    )
                } ?: messages["chapter"]
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_FILE))
        setOnAction {
            fire(NavigationRequestEvent(this@ChapterNarrationPage))
        }
    }

    override fun onDock() {
        super.onDock()
        navigator.dock(this, breadCrumb)
        viewModel.dock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
    }

    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])
    }

    override val root = stackpane {
        vbox {
            hbox {
                addClass("narration__header")

                label(workbookDataStore.activeWorkbookProperty.stringBinding { it?.target?.title }) {
                    addClass("narration__header-title")
                }
                region {
                    hgrow = Priority.ALWAYS
                }
                hbox {
                    addClass("narration__header-controls")

                    button {
                        addClass("btn", "btn--primary", "btn--borderless")
                        graphic = FontIcon(MaterialDesign.MDI_UNDO)

                        action {
                            println("Chapter has been reset")
                        }
                    }
                    add(
                        ChapterSelector().apply {
                            chapterTitleProperty.bind(workbookDataStore.activeChapterProperty.stringBinding {
                                it?.let {
                                    MessageFormat.format(
                                        messages["chapterTitle"],
                                        messages["chapter"],
                                        it.sort
                                    )
                                } ?: ""
                            })

                            prevDisabledProperty.set(true)

                            setOnPreviousChapter {
                                println("Previous chapter selected")
                            }
                            setOnNextChapter {
                                println("Next chapter selected")
                            }
                        }
                    )
                }
            }
            stackpane {
                addClass("narration__recording")
                alignment = Pos.CENTER

                hbox {
                    narrationrecordlistview(viewModel.recordedChunks) {
                        hgrow = Priority.ALWAYS

                        openInTextProperty.set(messages["openIn"])
                        recordAgainTextProperty.set(messages["recordAgain"])
                        loadingImageTextProperty.set(messages["loading"])

                        setCellFactory {
                            NarrationRecordCell().apply {
                                openInTextCellProperty.bind(openInTextProperty)
                                recordAgainTextCellProperty.bind(recordAgainTextProperty)
                                loadingImageTextCellProperty.bind(loadingImageTextProperty)

                                onOpenAppActionCellProperty.bind(onOpenAppActionProperty)
                                onRecordAgainActionCellProperty.bind(onRecordAgainActionProperty)
                            }
                        }

                        setOnOpenApp(viewModel::onChunkOpenIn)

                        setOnRecordAgain(viewModel::onRecordChunkAgain)
                    }

                    stackpane {
                        addClass("narration__volume-bar")

                        vbox {
                            addClass("narration__volume-bar__value")

                            maxHeight = 50.0
                        }
                    }
                }

                vbox {
                    addClass("narration__recording-tip")
                    alignment = Pos.CENTER_LEFT

                    label(messages["tip"]) {
                        addClass("narration__recording-tip-title")
                    }
                    label(messages["tipInfo"])

                    visibleProperty().bind(viewModel.recordedChunks.booleanBinding {
                        it.isEmpty()
                    })
                }
            }
            stackpane {
                addClass("narration__verses")

                narrationtextlistview(viewModel.chunks) {
                    addClass("narration__list")

                    viewModel.onCurrentVerseActionProperty.bind(onSelectedVerseActionProperty)
                    viewModel.floatingCardVisibleProperty.bind(cardIsOutOfViewProperty)

                    initialSelectedItemProperty.bind(viewModel.initialSelectedItemProperty)
                    viewModel.currentVerseLabelProperty.bind(selectionModel.selectedItemProperty().stringBinding {
                        it?.title
                    })

                    // Maybe instead of having 3 properties for recording status
                    // it's better to have only one property and change text according to the state
                    // in a view model???
                    beginRecordingTextProperty.set(messages["beginRecording"])
                    pauseRecordingTextProperty.set(messages["pauseRecording"])
                    resumeRecordingTextProperty.set(messages["resumeRecording"])
                    nextChunkTextProperty.set(messages["nextVerse"])

                    setCellFactory {
                        NarrationTextCell().apply {
                            beginRecordingTextCellProperty.bind(beginRecordingTextProperty)
                            pauseRecordingTextCellProperty.bind(pauseRecordingTextProperty)
                            resumeRecordingTextCellProperty.bind(resumeRecordingTextProperty)
                            nextChunkTextCellProperty.bind(nextChunkTextProperty)

                            onRecordActionCellProperty.bind(onRecordActionProperty)
                        }
                    }

                    setOnRecord(viewModel::onChunkRecord)
                }

                floatingnarrationcard {
                    floatingLabelProperty.bind(viewModel.currentVerseLabelProperty)
                    floatingCardVisibleProperty.bind(viewModel.floatingCardVisibleProperty)
                    onFloatingChunkActionProperty.bind(viewModel.onCurrentVerseActionProperty)

                    currentChunkTextProperty.set(messages["currentVerseTitle"])
                    currentVerseTextProperty.set(messages["verse"])
                    resumeTextProperty.set(messages["resume"])
                }
            }
        }
    }
}