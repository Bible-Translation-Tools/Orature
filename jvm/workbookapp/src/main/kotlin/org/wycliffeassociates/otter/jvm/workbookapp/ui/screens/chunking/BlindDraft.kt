package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.bindSingleChild
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BlindDraftViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

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
    }

    private fun buildTakesArea(): VBox {
        return VBox().apply {
            vbox {
                addClass("blind-draft-section", "blind-draft-section--top-indent")
                label(messages["best_take"]).addClass("h5", "h5--60")

                vbox {
                    addClass("take-list")
                    bindChildren(viewModel.selectedTake) { take ->
                        buildTakeCard(take)
                    }
                }
            }
            vbox {
                addClass("blind-draft-section", "blind-draft-section--top-indent")
                label(messages["available_takes"]).addClass("h5", "h5--60")
                vgrow = Priority.ALWAYS

                vbox {
                    addClass("take-list")
                    bindChildren(viewModel.availableTakes) { take ->
                        buildTakeCard(take)
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

    private fun buildTakeCard(take: TakeCardModel): HBox {
        return HBox().apply {
            addClass("take-card")
            simpleaudioplayer(take.audioPlayer) {
                hgrow = Priority.ALWAYS
                titleTextProperty.set(
                    MessageFormat.format(
                        messages["takeTitle"],
                        messages["take"],
                        take.take.number
                    )
                )
                enablePlaybackRateProperty.set(false)
                sideTextProperty.bind(remainingTimeProperty)
            }
            button {
                addClass("btn", "btn--icon", "btn--borderless")
                tooltip(messages["options"])
                graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)
            }
            button {
                addClass("btn", "btn--icon")
                tooltip(messages["select"])
                togglePseudoClass("active", take.selected)

                graphic = FontIcon(MaterialDesign.MDI_STAR_OUTLINE)
                isMouseTransparent = take.selected
                isFocusTraversable = !take.selected

                action {
                    viewModel.selectTake(take.take)
                }
            }
        }
    }
}