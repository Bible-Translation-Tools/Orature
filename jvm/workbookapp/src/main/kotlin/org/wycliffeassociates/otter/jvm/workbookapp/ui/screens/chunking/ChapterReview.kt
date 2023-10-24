package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.scene.control.Slider
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerWaveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.PeerEditViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class ChapterReview : Fragment() {
    val viewModel: PeerEditViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()
    private lateinit var waveform: MarkerWaveform

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
            val container = this
            waveform = MarkerWaveform().apply {
                vgrow = Priority.ALWAYS
                themeProperty.bind(settingsViewModel.appColorMode)
                positionProperty.bind(viewModel.positionProperty)
                clip = Rectangle().apply {
                    widthProperty().bind(container.widthProperty())
                    heightProperty().bind(container.heightProperty())
                }
                setOnWaveformClicked { viewModel.pause() }
                setOnWaveformDragReleased { deltaPos ->
                    val deltaFrames = pixelsToFrames(deltaPos)
                    val curFrames = viewModel.getLocationInFrames()
                    val duration = viewModel.getDurationInFrames()
                    val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                    viewModel.seek(final)
                }

                viewModel.subscribeOnWaveformImages = ::subscribeOnWaveformImages
                viewModel.cleanUpWaveform = ::freeImages
            }
            add(waveform)

            val scrollbarSlider = createAudioScrollbarSlider().also {
                viewModel.slider = it
            }
            add(scrollbarSlider)

            hbox {
                addClass("consume__bottom", "recording__bottom-section")
                hbox {
                    addClass("chunking-bottom__media-btn-group")

                    button {
                        addClass("btn", "btn--icon")
                        graphic = FontIcon(MaterialDesign.MDI_SKIP_PREVIOUS)

//                        action { viewModel.seekPrevious() }
                    }
                    button {
                        addClass("btn", "btn--icon")
                        val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
                        val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
                        tooltipProperty().bind(
                            viewModel.isPlayingProperty.objectBinding {
                                togglePseudoClass("active", it == true)
                                if (it == true) {
                                    graphic = pauseIcon
                                    Tooltip(messages["pause"])
                                } else {
                                    graphic = playIcon
                                    Tooltip(messages["play"])
                                }
                            }
                        )

//                        action { viewModel.mediaToggle() }
                    }
                    button {
                        addClass("btn", "btn--icon")
                        graphic = FontIcon(MaterialDesign.MDI_SKIP_NEXT)

//                        action { viewModel.seekNext() }
                    }
                }
                region { hgrow = Priority.ALWAYS }
                button(messages["next_chapter"]) {

                }
            }
        }
    }

    private fun subscribeOnWaveformImages() {
        viewModel.waveform
            .observeOnFx()
            .subscribe {
                waveform.addWaveformImage(it)
            }
            .addTo(viewModel.disposable)
    }

    private fun createAudioScrollbarSlider(): Slider {
        return AudioSlider().apply {
            hgrow = Priority.ALWAYS
            colorThemeProperty.bind(settingsViewModel.selectedThemeProperty)
            setPixelsInHighlightFunction { viewModel.pixelsInHighlight(it) }
            player.bind(viewModel.waveformAudioPlayerProperty)
            secondsToHighlightProperty.set(SECONDS_ON_SCREEN)
        }
    }
}