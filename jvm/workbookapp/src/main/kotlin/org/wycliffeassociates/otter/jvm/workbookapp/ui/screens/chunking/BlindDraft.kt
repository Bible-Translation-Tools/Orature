package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.TakeSelectionAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChunkTakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.ChunkTakeEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.TakeAction
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BlindDraftViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class BlindDraft : Fragment() {

    val viewModel: BlindDraftViewModel by inject()
    val recorderViewModel: RecorderViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()
    private val mainSectionProperty = SimpleObjectProperty<Node>(null)
    private val takesView = buildTakesArea()
    private val recordingView = buildRecordingArea()

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
            }
        }
        center = stackpane {
            bindSingleChild(mainSectionProperty)
        }
    }

    init {
        tryImportStylesheet("/css/recording-screen.css")
        tryImportStylesheet("/css/popup-menu.css")
        subscribeEvents()
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

                vbox {
                    addClass("take-list")
                    animationMediator.itemList.bind(childrenUnmodifiable) { it }
                    bindChildren(viewModel.availableTakes) { take ->
                        ChunkTakeCard(take).apply {
                            animationMediatorProperty.set(animationMediator)
                        }
                    }
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
        return RecordingSection(recorderViewModel).apply {
            onRecordingFinish = { result ->
                viewModel.onRecordFinish(result)
                mainSectionProperty.set(takesView)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        mainSectionProperty.set(takesView)
        viewModel.dockBlindDraft()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undockBlindDraft()
    }

    private fun subscribeEvents() {
        subscribe<ChunkTakeEvent> {
            when (it.action) {
                TakeAction.SELECT -> viewModel.selectTake(it.take)
//                TakeAction.EDIT -> viewModel.editTake(it.take)
                TakeAction.DELETE -> viewModel.deleteTake(it.take)
            }
        }
    }
}