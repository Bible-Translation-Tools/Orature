package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BlindDraftViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class BlindDraft : Fragment() {

    val viewModel: BlindDraftViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()

    override val root = vbox {
        addClass("blind-draft")

        vbox {
            addClass("blind-draft-section")
            label(viewModel.chunkTitleProperty).addClass("h4", "h4--80")
            simpleaudioplayer {
                playerProperty.bind(viewModel.sourceAudioProperty)
                enablePlaybackRateProperty.set(true)
            }
        }
        vbox {
            addClass("blind-draft-section", "blind-draft-section--top-indent")
            label(messages["best_take"]).addClass("h5")
        }
        vbox {
            addClass("blind-draft-section", "blind-draft-section--top-indent")
            label(messages["available_takes"]).addClass("h5")
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