package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BlindDraftViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class BlindDraft : Fragment() {

    val viewModel: BlindDraftViewModel by inject()
    val translationViewModel: TranslationViewModel2 by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    override val root = vbox {
        addClass("blind-draft")

        vbox {
            addClass("blind-draft-section")
            label(viewModel.chunkTitleProperty).addClass("h4", "h4--80")
            simpleaudioplayer {
                playerProperty.bind(viewModel.sourcePlayerProperty)
                enablePlaybackRateProperty.set(true)
                onPlaybackProgressChanged = { location ->
                    viewModel.markerModelProperty.value?.let { markerModel ->
                        val nearestMarkerFrame = markerModel.seekCurrent(location.toInt())
                        val currentMarker = markerModel.markers.find { it.frame == nearestMarkerFrame }
                        val index = currentMarker?.let { markerModel.markers.indexOf(it) } ?: -1
                        translationViewModel.currentMarkerProperty.set(index)
                    }
                }
                sideTextProperty.set(messages["sourceAudio"])
            }
        }
        vbox {
            addClass("blind-draft-section", "blind-draft-section--top-indent")
            label(messages["best_take"]).addClass("h5", "h5--60")
        }
        vbox {
            addClass("blind-draft-section", "blind-draft-section--top-indent")
            label(messages["available_takes"]).addClass("h5", "h5--60")
            vgrow = Priority.ALWAYS
        }
        hbox {
            addClass("consume__bottom")
            button(messages["new_recording"]) {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.dockBlindDraft()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undockBlindDraft()
    }
}