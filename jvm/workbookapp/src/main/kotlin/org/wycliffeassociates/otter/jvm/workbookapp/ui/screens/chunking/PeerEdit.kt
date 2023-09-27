package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.scene.control.Slider
import javafx.scene.layout.Priority
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.ScrollingWaveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.PeerEditViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class PeerEdit : Fragment() {

    val viewModel: PeerEditViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var slider: Slider
    private lateinit var waveform: ScrollingWaveform

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
        center = vbox {
            waveform = ScrollingWaveform().apply {
                themeProperty.bind(settingsViewModel.appColorMode)
                positionProperty.bind(viewModel.positionProperty)
                clip = Rectangle().apply {
                    widthProperty().bind(this@vbox.widthProperty())
                    heightProperty().bind(this@vbox.heightProperty())
                }
                setOnWaveformClicked { viewModel.pause() }
                setOnWaveformDragReleased { deltaPos ->
                    val deltaFrames = pixelsToFrames(deltaPos)
                    val curFrames = viewModel.getLocationInFrames()
                    val duration = viewModel.getDurationInFrames()
                    val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                    viewModel.seek(final)
                }

                viewModel.cleanUpWaveform = ::freeImages
            }
            slider = createAudioScrollbarSlider()
            viewModel.slider = slider
            add(waveform)
            add(slider)
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
                        messages["play"]
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
            button(messages["record"]) {
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
        viewModel.subscribeOnWaveformImages = ::subscribeOnWaveformImages
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undockPeerEdit()
    }

    private fun subscribeOnWaveformImages() {
        viewModel.waveform
            .observeOnFx()
            .subscribe {
                waveform.addWaveformImage(it)
            }
            .addTo(viewModel.compositeDisposable)
    }

    private fun createAudioScrollbarSlider(): Slider {
        return AudioSlider().apply {
            hgrow = Priority.ALWAYS
            colorThemeProperty.bind(settingsViewModel.selectedThemeProperty)
            setPixelsInHighlightFunction { viewModel.pixelsInHighlight(it) }
            player.bind(viewModel.targetPlayerProperty)
            secondsToHighlightProperty.set(SECONDS_ON_SCREEN)
        }
    }
}