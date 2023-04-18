package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.github.thomasnield.rxkotlinfx.toLazyBinding
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.geometry.NodeOrientation
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterSelector
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.narration.floatingnarrationcard
import org.wycliffeassociates.otter.jvm.controls.narration.narrationrecordlistview
import org.wycliffeassociates.otter.jvm.controls.narration.narrationtextlistview
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationRecordCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterNarrationViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat
import java.util.*

class ChapterNarrationPage : View() {
    private val logger = LoggerFactory.getLogger(ChapterNarrationPage::class.java)

    private val viewModel: ChapterNarrationViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
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

    private val pluginOpenedPage: PluginOpenedPage

    init {
        pluginOpenedPage = createPluginOpenedPage()
        workspace.subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                workspace.dock(pluginOpenedPage)
                viewModel.openSourcePlayer()
            }
        }
        workspace.subscribe<PluginClosedEvent> {
            (workspace.dockedComponentProperty.value as? PluginOpenedPage)?.let {
                workspace.navigateBack()
            }
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
        createSnackBar()

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

                    menubutton {
                        addClass("btn", "btn--primary", "btn--borderless", "wa-menu-button")
                        graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL)

                        item(messages["undoAction"]) {
                            graphic = FontIcon(MaterialDesign.MDI_UNDO)
                            action { viewModel.onUndoAction() }

                            enableWhen(viewModel.narrationHistory.hasUndoProperty)
                        }
                        item(messages["redoAction"]) {
                            graphic = FontIcon(MaterialDesign.MDI_REDO)
                            action { viewModel.onRedoAction() }

                            enableWhen(viewModel.narrationHistory.hasRedoProperty)
                        }
                        item(messages["openChapterIn"]) {
                            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                            action { viewModel.onChapterOpenIn() }

                            disableProperty().bind(workbookDataStore.activeChapterProperty.booleanBinding {
                                it?.audio?.getAllTakes()?.firstOrNull { take ->
                                    take.deletedTimestamp.value?.value == null
                                } == null
                            })
                        }
                        item(messages["editVerseMarkers"]) {
                            graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                            action { viewModel.onEditVerseMarkers() }

                            disableProperty().bind(workbookDataStore.activeChapterProperty.booleanBinding {
                                it?.audio?.getAllTakes()?.firstOrNull { take ->
                                    take.deletedTimestamp.value?.value == null
                                } == null
                            })
                        }
                        item(messages["restartChapter"]) {
                            graphic = FontIcon(MaterialDesign.MDI_DELETE)
                            action { viewModel.onChapterReset() }
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

                            prevDisabledProperty.bind(viewModel.hasPreviousChapter.not())
                            nextDisabledProperty.bind(viewModel.hasNextChapter.not())

                            setOnPreviousChapter {
                                viewModel.previousChapter()
                            }
                            setOnNextChapter {
                                viewModel.nextChapter()
                            }
                        }
                    )
                }
            }
            stackpane {
                addClass("narration__recording")

                hbox {
                    narrationrecordlistview(viewModel.recordedChunks) {
                        nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
                        hgrow = Priority.ALWAYS
                        alignment = Pos.CENTER_LEFT

                        setCellFactory {
                            NarrationRecordCell(
                                messages["openIn"],
                                messages["recordAgain"],
                                messages["loading"],
                                messages["goToVerse"]
                            )
                        }

                        viewModel.onPlaybackStarted = {
                            scrollTo(it)
                        }

                        viewModel.onScrollToChunk = {
                            scrollTo(it)
                        }

                        isRecordingProperty.bind(viewModel.recordStartedProperty.and(viewModel.recordPausedProperty.not()))
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
                    }.and(viewModel.recordStartedProperty.not()))
                }
            }
            stackpane {
                addClass("narration__verses")

                narrationtextlistview(viewModel.allSortedChunks) {
                    addClass("narration__list")

                    viewModel.onCurrentVerseActionProperty.bind(onSelectedVerseActionProperty)
                    viewModel.floatingCardVisibleProperty.bind(cardIsOutOfViewProperty)

                    initialSelectedItemProperty.bind(viewModel.initialSelectedItemProperty)
                    viewModel.currentVerseLabelProperty.bind(selectionModel.selectedItemProperty().stringBinding {
                        it?.title
                    })

                    viewModel.onScrollToChunk = {
                        selectionModel.select(it)
                        scrollTo(it)
                    }

                    setCellFactory {
                        NarrationTextCell(messages["nextVerse"])
                    }
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

    private fun createSnackBar() {
        viewModel
            .snackBarObservable
            .doOnError { e ->
                logger.error("Error in creating no plugin snackbar", e)
            }
            .subscribe { pluginErrorMessage ->
                SnackbarHandler.enqueue(
                    JFXSnackbar.SnackbarEvent(
                        JFXSnackbarLayout(
                            pluginErrorMessage,
                            messages["addApp"].uppercase(Locale.getDefault())
                        ) {
                            audioPluginViewModel.addPlugin(true, false)
                        },
                        Duration.millis(5000.0),
                        null
                    )
                )
            }
    }

    private fun createPluginOpenedPage(): PluginOpenedPage {
        // Plugin active cover
        return find<PluginOpenedPage>().apply {
            dialogTitleProperty.bind(viewModel.dialogTitleBinding())
            dialogTextProperty.bind(viewModel.dialogTextBinding())
            playerProperty.bind(viewModel.sourceAudioPlayerProperty)
            targetAudioPlayerProperty.bind(workbookDataStore.targetAudioProperty.objectBinding { it?.player })
            audioAvailableProperty.bind(viewModel.sourceAudioAvailableProperty)
            licenseProperty.bind(workbookDataStore.sourceLicenseProperty)
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            sourceContentTitleProperty.bind(workbookDataStore.activeTitleBinding())
            orientationProperty.bind(settingsViewModel.orientationProperty)
            sourceOrientationProperty.bind(settingsViewModel.sourceOrientationProperty)

            sourceSpeedRateProperty.bind(
                workbookDataStore.activeWorkbookProperty.select {
                    it.translation.sourceRate.toLazyBinding()
                }
            )

            targetSpeedRateProperty.bind(
                workbookDataStore.activeWorkbookProperty.select {
                    it.translation.targetRate.toLazyBinding()
                }
            )
            sourceTextZoomRateProperty.bind(
                workbookDataStore.sourceTextZoomRateProperty
            )
        }
    }
}