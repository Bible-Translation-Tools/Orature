package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.TakeSelectionAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChunkTakeCard
import org.wycliffeassociates.otter.jvm.controls.event.ChunkTakeEvent
import org.wycliffeassociates.otter.jvm.controls.event.RedoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.event.TakeAction
import org.wycliffeassociates.otter.jvm.controls.event.UndoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BlindDraftViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class BlindDraft : View() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val viewModel: BlindDraftViewModel by inject()
    val recorderViewModel: RecorderViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()
    private val mainSectionProperty = SimpleObjectProperty<Node>(null)
    private val takesView = buildTakesArea()
    private val recordingView = buildRecordingArea()
    private val hideSourceAudio = mainSectionProperty.booleanBinding { it == recordingView }
    private val eventSubscriptions = mutableListOf<EventRegistration>()
    private val listenerDisposers = mutableListOf<ListenerDisposer>()

    override val root = borderpane {
        addClass("blind-draft")

        top {
            vbox {
                addClass("blind-draft-section")
                label(viewModel.chunkTitleProperty).addClass("h4", "h4--80")
                simpleaudioplayer {
                    playerProperty.bind(viewModel.sourcePlayerProperty)
                    enablePlaybackRateProperty.set(true)
                    sideTextProperty.set(messages["sourceAudio"])
                }
                visibleWhen { hideSourceAudio.not() }
                managedWhen(visibleProperty())
            }
        }
        centerProperty().bind(mainSectionProperty)
    }

    init {
        tryImportStylesheet("/css/recording-screen.css")
        tryImportStylesheet("/css/popup-menu.css")
    }

    private fun buildTakesArea(): VBox {
        val animationMediator = TakeSelectionAnimationMediator<ChunkTakeCard>()

        return VBox().apply {
            vbox {
                addClass("blind-draft-section", "blind-draft-section--top-indent")
                label(messages["best_take"]).addClass("h5", "h5--60")

                vbox {
                    addClass("take-list")
                    bindChildren(viewModel.selectedTake) { take ->
                        ChunkTakeCard(take).apply {
                            animationMediator.selectedNode = this
                            animationMediatorProperty.set(animationMediator)
                        }
                    }
                }
            }
            vbox {
                addClass("blind-draft-section", "blind-draft-section--top-indent")
                label(messages["available_takes"]).addClass("h5", "h5--60")
                vgrow = Priority.ALWAYS

                scrollpane {
                    vgrow = Priority.ALWAYS
                    isFitToWidth = true
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    vbox {
                        addClass("take-list")
                        animationMediator.nodeList = childrenUnmodifiable
                        bindChildren(viewModel.availableTakes) { take ->
                            ChunkTakeCard(take).apply {
                                animationMediatorProperty.set(animationMediator)
                            }
                        }
                    }

                    runLater { customizeScrollbarSkin() }
                }
            }
            hbox {
                addClass("consume__bottom")
                button(messages["new_recording"]) {
                    addClass("btn", "btn--primary")
                    graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)

                    action {
                        viewModel.onRecordNew()
                        mainSectionProperty.set(recordingView)
                        recorderViewModel.onViewReady(takesView.width.toInt()) // use the width of the existing component
                        recorderViewModel.toggle()
                    }
                }
            }
        }
    }

    private fun buildRecordingArea(): RecordingSection {
        return RecordingSection().apply {
            recorderViewModel.waveformCanvas = waveformCanvas
            recorderViewModel.volumeCanvas = volumeCanvas
            isRecordingProperty.bind(recorderViewModel.recordingProperty)

            setToggleRecordingAction {
                recorderViewModel.toggle()
            }

            setCancelAction {
                recorderViewModel.cancel()
                viewModel.onRecordFinish(RecorderViewModel.Result.CANCELLED)
                mainSectionProperty.set(takesView)
            }

            setSaveAction {
                val result = recorderViewModel.saveAndQuit()
                viewModel.onRecordFinish(result)
                mainSectionProperty.set(takesView)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        logger.info("Blind Draft docked.")
        recorderViewModel.waveformCanvas = recordingView.waveformCanvas
        recorderViewModel.volumeCanvas = recordingView.volumeCanvas
        mainSectionProperty.set(takesView)
        viewModel.dockBlindDraft()
        subscribeEvents()
    }

    override fun onUndock() {
        super.onUndock()
        logger.info("Blind Draft undocked.")
        unsubscribeEvents()
        viewModel.undockBlindDraft()
        recorderViewModel.cancel()
    }

    private fun subscribeEvents() {
        viewModel.currentChunkProperty.onChangeWithDisposer { selectedChunk ->
            // clears recording screen if another chunk is selected
            if (selectedChunk != null && mainSectionProperty.value == recordingView) {
                recorderViewModel.cancel()
                mainSectionProperty.set(takesView)
            }
        }.also { listenerDisposers.add(it) }
        
        subscribe<ChunkTakeEvent> {
            when (it.action) {
                TakeAction.SELECT -> viewModel.onSelectTake(it.take)
                TakeAction.DELETE -> viewModel.onDeleteTake(it.take)
                TakeAction.EDIT -> {
                    // TODO()
                }
            }
        }.also { eventSubscriptions.add(it) }

        subscribe<UndoChunkingPageEvent> {
            viewModel.undo()
        }.also { eventSubscriptions.add(it) }

        subscribe<RedoChunkingPageEvent> {
            viewModel.redo()
        }.also { eventSubscriptions.add(it) }
    }

    private fun unsubscribeEvents() {
        eventSubscriptions.forEach { it.unsubscribe() }
        eventSubscriptions.clear()
        listenerDisposers.forEach { it.dispose() }
        listenerDisposers.clear()
    }
}