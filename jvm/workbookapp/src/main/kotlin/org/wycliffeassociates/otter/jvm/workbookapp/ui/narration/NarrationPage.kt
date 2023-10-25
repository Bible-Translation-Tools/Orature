package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.toLazyBinding
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSnackbarLayout
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.util.Duration
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.event.BeginRecordingEvent
import org.wycliffeassociates.otter.jvm.controls.event.ChapterReturnFromPluginEvent
import org.wycliffeassociates.otter.jvm.controls.event.NextVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.OpenChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.OpenInAudioPluginEvent
import org.wycliffeassociates.otter.jvm.controls.event.PauseEvent
import org.wycliffeassociates.otter.jvm.controls.event.PauseRecordingEvent
import org.wycliffeassociates.otter.jvm.controls.event.PlayChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.PlayVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordAgainEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.ResumeRecordingEvent
import org.wycliffeassociates.otter.jvm.controls.event.SaveRecordingEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.NarrationMarkerChangedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationRedoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationResetChapterEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationUndoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.util.*

class NarrationPage : View() {
    private val logger = LoggerFactory.getLogger(NarrationPage::class.java)

    private val viewModel: NarrationViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()

    private val pluginOpenedPage: PluginOpenedPage

    private val eventSubscriptions = mutableListOf<EventRegistration>()

    private lateinit var narrationHeader: NarrationHeader
    private lateinit var audioWorkspaceView: AudioWorkspaceView
    private lateinit var narrationToolbar: NarrationToolBar
    private lateinit var teleprompterView: TeleprompterView
    private var chapterGrid: ChapterGrid

    private val chapterList: ObservableList<ChapterGridItemData> = observableListOf()

    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])
        tryImportStylesheet("/css/chapter-grid.css")

        pluginOpenedPage = createPluginOpenedPage()
        chapterGrid = ChapterGrid(chapterList)

        // TODO: set the default selected chapter to actual true/false
        viewModel.chapterList.onChange {
            runLater {
                chapterGrid.clear()
                chapterList.setAll(emptyList())
                chapterList.setAll(viewModel.chapterList.map {
                    ChapterGridItemData(
                        it.sort,
                        SimpleBooleanProperty(false)
                    )
                })
            }
        }

    }


    override val root = stackpane {
        addClass(ColorTheme.LIGHT.styleClass)

        createSnackBar()

        narrationHeader = find()
        audioWorkspaceView = find()
        narrationToolbar = find()
        teleprompterView = find()

        borderpane {
            top = narrationHeader.root
            center = borderpane {
                style {
                    padding = box(0.px, 0.px, 1.px, 0.px)
                    backgroundColor += Color.WHITE
                }
                center = audioWorkspaceView.root
                bottom = narrationToolbar.root
            }
            bottom = teleprompterView.root
        }

        borderpane {
            visibleProperty().bind(viewModel.chapterGridOpen)
            managedProperty().bind(viewModel.chapterGridOpen)

            right = vbox {
                maxWidth = 500.0
                minWidth = 500.0
                style {
                    // TODO: don't hardcode the paddingTop and paddingRight here. Put them in the stylesheet
                    paddingTop = 80
                    paddingRight = 16
                }
                hgrow = Priority.NEVER // TODO: see if I can remove this
                add(chapterGrid)
            }

            setOnMouseClicked { event ->
                val rightControlBounds = right.localToScene(right.boundsInLocal)
                val clickPoint = javafx.geometry.Point2D(event.sceneX, event.sceneY)

                // Check if the click is outside the bounds of the right control
                if (!rightControlBounds.contains(clickPoint)) {
                    viewModel.toggleChapterGridOpen()
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        subscribeToEvents()
        viewModel.onDock()
        narrationHeader.onDock()
        audioWorkspaceView.onDock()
        teleprompterView.onDock()
    }

    override fun onUndock() {
        super.onUndock()
        unsubscribeFromEvents()
        viewModel.onUndock()
        narrationHeader.onUndock()
        audioWorkspaceView.onUndock()
        teleprompterView.onUndock()
    }

    private fun subscribeToEvents() {
        subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                workspace.dock(pluginOpenedPage)
            }
        }.let { eventSubscriptions.add(it) }

        subscribe<SnackBarEvent> {
            viewModel.snackBarMessage(it.message)
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationResetChapterEvent> {
            viewModel.resetChapter()
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationUndoEvent> {
            viewModel.undo()
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationRedoEvent> {
            viewModel.redo()
        }.let { eventSubscriptions.add(it) }

        subscribe<RecordVerseEvent> {
            viewModel.toggleRecording(it.index)
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<BeginRecordingEvent> {
            viewModel.toggleRecording(it.index)
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<PauseRecordingEvent> {
            viewModel.toggleRecording(it.index)
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<ResumeRecordingEvent> {
            viewModel.toggleRecording(it.index)
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationMarkerChangedEvent> {
            logger.info("Received Narration Moved event")
            viewModel.moveMarker(it.index, it.delta)
        }.let { eventSubscriptions.add(it) }

        subscribe<NextVerseEvent> {
            viewModel.onNext(it.index)
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<PlayVerseEvent> {
            viewModel.play(it.verse)
        }.let { eventSubscriptions.add(it) }

        subscribe<PlayChapterEvent> {
            viewModel.playAll()
        }.let { eventSubscriptions.add(it) }

        subscribe<PauseEvent> {
            viewModel.pausePlayback()
        }.let { eventSubscriptions.add(it) }

        subscribe<RecordAgainEvent> {
            viewModel.recordAgain(it.index)
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<SaveRecordingEvent> {
            viewModel.saveRecording(it.index)
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<OpenInAudioPluginEvent> {
            viewModel.openInAudioPlugin(it.index)
        }.let { eventSubscriptions.add(it) }

        subscribe<ChapterReturnFromPluginEvent> {
            viewModel.onChapterReturnFromPlugin()
        }.let { eventSubscriptions.add(it) }

        subscribe<OpenChapterEvent> {
            viewModel.loadChapter(it.chapter)
        }.let { eventSubscriptions.add(it) }
    }

    private fun unsubscribeFromEvents() {
        eventSubscriptions.forEach { it.unsubscribe() }
        eventSubscriptions.clear()
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
                            audioPluginViewModel.addPlugin(record = true, edit = false)
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
        }
    }
}

class SnackBarEvent(val message: String) : FXEvent()