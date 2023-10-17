package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.scene.control.Slider
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.event.MarkerDeletedEvent
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerWaveform
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import tornadofx.*

class Chunking : Fragment() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val viewModel: ChunkingViewModel by inject()
    val translationViewModel: TranslationViewModel2 by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var waveform: MarkerWaveform
    private lateinit var slider: Slider

    var cleanUpWaveform: () -> Unit = {}

    override fun onDock() {
        super.onDock()
        logger.info("Chunking docked")

        viewModel.subscribeOnWaveformImages = ::subscribeOnWaveformImages
        viewModel.onDockChunking()
        viewModel.initializeAudioController(slider)
        waveform.markers.bind(viewModel.markers) { it }

        subscribe<MarkerDeletedEvent> { viewModel.deleteMarker(it.markerId) }
    }

    override fun onUndock() {
        super.onUndock()
        logger.info("Chunking undocked")
        cleanUpWaveform()
        viewModel.onUndockChunking()
    }

    private fun subscribeOnWaveformImages() {
        viewModel.waveform
            .observeOnFx()
            .subscribe {
                waveform.addWaveformImage(it)
            }
            .addTo(viewModel.compositeDisposable)
    }

    override val root = vbox {
        borderpane {
            vgrow = Priority.ALWAYS

            center = VBox().apply {
                MarkerWaveform().apply {
                    waveform = this
                    addClass("consume__scrolling-waveform")
                    vgrow = Priority.ALWAYS
                    clip = Rectangle().apply {
                        widthProperty().bind(this@vbox.widthProperty())
                        heightProperty().bind(this@vbox.heightProperty())
                    }
                    themeProperty.bind(settingsViewModel.appColorMode)
                    positionProperty.bind(viewModel.positionProperty)
                    canMoveMarkerProperty.set(true)
                    imageWidthProperty.bind(viewModel.imageWidthProperty)

                    setUpWaveformActionHandlers()
                    cleanUpWaveform = ::freeImages

                    // Marker stuff
                    this.markers.bind(viewModel.markers) { it }
                }
                slider = createAudioScrollbarSlider()
                add(waveform)
                add(slider)
            }
            bottom = hbox {
                addClass("consume__bottom")
                button(messages["addChunk"]) {
                    addClass("btn", "btn--primary", "consume__btn")
                    tooltip(text)
                    graphic = FontIcon(MaterialDesign.MDI_PLUS)

                    action {
                        viewModel.placeMarker()
                    }
                }
                region { hgrow = Priority.ALWAYS }
                hbox {
                    addClass("chunking-bottom__media-btn-group")
                    button {
                        addClass("btn", "btn--icon")
                        graphic = FontIcon(MaterialDesign.MDI_SKIP_PREVIOUS)

                        action { viewModel.seekPrevious() }
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
                                    Tooltip(messages["playSource"])
                                }
                            }
                        )

                        action { viewModel.mediaToggle() }
                    }
                    button {
                        addClass("btn", "btn--icon")
                        graphic = FontIcon(MaterialDesign.MDI_SKIP_NEXT)

                        action { viewModel.seekNext() }
                    }
                }
            }
        }
    }

    private fun setUpWaveformActionHandlers() {
        waveform.apply {
            setOnSeekNext { viewModel.seekNext() }
            setOnSeekPrevious { viewModel.seekPrevious() }
            setOnPlaceMarker { viewModel.placeMarker() }
            setOnWaveformClicked { viewModel.pause() }
            setOnWaveformDragReleased { deltaPos ->
                val deltaFrames = pixelsToFrames(deltaPos)
                val curFrames = viewModel.getLocationInFrames()
                val duration = viewModel.getDurationInFrames()
                val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                viewModel.seek(final)
            }
            setOnRewind(viewModel::rewind)
            setOnFastForward(viewModel::fastForward)
            setOnToggleMedia(viewModel::mediaToggle)
            setOnResumeMedia(viewModel::resumeMedia)

            setOnPositionChanged { _, _ ->
                // markers moved = dirty
                viewModel.dirtyMarkers.set(true)
            }
        }
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