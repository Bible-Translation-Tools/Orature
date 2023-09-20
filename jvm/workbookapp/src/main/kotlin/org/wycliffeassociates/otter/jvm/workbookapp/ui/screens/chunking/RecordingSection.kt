package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel.Result
import tornadofx.*
import tornadofx.FX.Companion.messages

class RecordingSection(private val viewModel: RecorderViewModel) : BorderPane() {

    var onRecordingFinish: (Result) -> Unit = {}

    private val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
    private val resumeIcon = FontIcon(MaterialDesign.MDI_MICROPHONE)

    init {
        center = viewModel.waveformCanvas
        right = viewModel.volumeCanvas
        bottom = hbox {
            addClass("consume__bottom")
            button {
                addClass("btn", "btn--primary", "consume__btn")
                textProperty().bind(viewModel.recordingProperty.stringBinding {
                    togglePseudoClass("active", it == true)
                    if (it == true) {
                        graphic = pauseIcon
                        messages["pause"]
                    } else {
                        graphic = resumeIcon
                        messages["resume"]
                    }
                })
                tooltip { textProperty().bind(this@button.textProperty()) }

                action {
                    viewModel.toggle()
                }
            }
            button(messages["save"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE)

                visibleWhen { viewModel.recordingProperty.not() }
                managedWhen(visibleProperty())

                action {
                    viewModel.saveAndQuit(onRecordingFinish)
                }
            }
            region { hgrow = Priority.ALWAYS }
            button(messages["cancel"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)

                visibleWhen { viewModel.recordingProperty.not() }
                managedWhen(visibleProperty())

                action {
                    viewModel.cancel(onRecordingFinish)
                }
            }
        }
    }
}
