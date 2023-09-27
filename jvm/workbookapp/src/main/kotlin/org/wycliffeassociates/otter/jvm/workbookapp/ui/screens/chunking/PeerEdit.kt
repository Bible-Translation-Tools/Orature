package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.rxkotlin.addTo
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.waveform.ScrollingWaveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.PeerEditViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class PeerEdit : Fragment() {

    val viewModel: PeerEditViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    var cleanUpWaveform: () -> Unit = {}

    override val root = borderpane {
        top = vbox {
            addClass("blind-draft-section")
            label(viewModel.chunkTitleProperty).addClass("h4", "h4--80")
            simpleaudioplayer {
                playerProperty.bind(viewModel.sourcePlayerProperty)
                enablePlaybackRateProperty.set(true)
                sideTextProperty.set(messages["sourceAudio"])
            }
        }
        center = ScrollingWaveform().apply {
            themeProperty.bind(settingsViewModel.appColorMode)
            positionProperty.bind(viewModel.positionProperty)

            cleanUpWaveform = ::freeImages
        }
        bottom = hbox {
            addClass("consume__bottom", "recording__bottom-section")
            button {
                addClass("btn", "btn--primary", "consume__btn")
                val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
                val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
                textProperty().bind(viewModel.isPlayingProperty.stringBinding {
                    togglePseudoClass("active", it == true)
                    if (it == true) {
                        graphic = pauseIcon
                        messages["pause"]
                    } else {
                        graphic = playIcon
                        messages["playSource"]
                    }
                })

                action {
                    viewModel.toggleAudio()
                }
            }
            button(messages["confirm"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE)

                visibleWhen { viewModel.isPlayingProperty.not() }

                action {

                }
            }
            region { hgrow = Priority.ALWAYS }
            button(messages["cancel"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)

                disableWhen { viewModel.isPlayingProperty.not() }

                action {

                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.dockPeerEdit()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undockPeerEdit()
    }

    private fun subscribeOnWaveformImages() {
        viewModel.waveform
            .observeOnFx()
            .subscribe {
                (root.center as ScrollingWaveform).addWaveformImage(it)
            }
            .addTo(viewModel.compositeDisposable)
    }
}