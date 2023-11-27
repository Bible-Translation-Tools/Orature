package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.animation.AnimationTimer
import javafx.scene.control.Slider
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.event.GoToNextChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.MarkerDeletedEvent
import org.wycliffeassociates.otter.jvm.controls.event.MarkerMovedEvent
import org.wycliffeassociates.otter.jvm.controls.event.RedoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.event.UndoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.startAnimationTimer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterReviewViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class ChapterReview : View() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val viewModel: ChapterReviewViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var waveform: MarkerWaveform
    private lateinit var scrollbarSlider: Slider
    private var timer: AnimationTimer? = null
    private var cleanUpWaveform: () -> Unit = {}

    private val eventSubscriptions = mutableListOf<EventRegistration>()

    override val root = borderpane {
        top = vbox {
            addClass("blind-draft-section")
            label(viewModel.chapterTitleProperty).addClass("h4", "h4--80")
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
                setOnWaveformClicked { viewModel.pauseAudio() }
                setOnWaveformDragReleased { deltaPos ->
                    val deltaFrames = pixelsToFrames(deltaPos)
                    val curFrames = viewModel.getLocationInFrames()
                    val duration = viewModel.getDurationInFrames()
                    val final = Utils.clamp(0, curFrames - deltaFrames, duration)
                    viewModel.seek(final)
                }

                viewModel.subscribeOnWaveformImages = ::subscribeOnWaveformImages
                cleanUpWaveform = ::freeImages

                markers.bind(viewModel.markers) { it }
            }
            scrollbarSlider = createAudioScrollbarSlider()
            add(waveform)
            add(scrollbarSlider)

            hbox {
                addClass("consume__bottom", "chunking-bottom__media-btn-group")
                button(messages["addVerse"]) {
                    addClass("btn", "btn--primary", "consume__btn")
                    tooltip(text)
                    graphic = FontIcon(MaterialDesign.MDI_PLUS)
                    disableWhen {
                        viewModel.markersPlacedCountProperty.isEqualTo(viewModel.totalMarkersProperty)
                    }

                    action {
                        viewModel.placeMarker()
                    }
                }
                label(viewModel.markerProgressCounterProperty) {
                    addClass("normal-text")
                }
                region { hgrow = Priority.ALWAYS }
                hbox {
                    addClass("chunking-bottom__media-btn-group")

                    button {
                        addClass("btn", "btn--icon")
                        graphic = FontIcon(MaterialDesign.MDI_SKIP_PREVIOUS)
                        tooltip(messages["previousChunk"])

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
                                    Tooltip(messages["play"])
                                }
                            }
                        )

                        action { viewModel.mediaToggle() }
                    }
                    button {
                        addClass("btn", "btn--icon")
                        graphic = FontIcon(MaterialDesign.MDI_SKIP_NEXT)
                        tooltip(messages["nextChunk"])

                        action { viewModel.seekNext() }
                    }
                    button(messages["nextChapter"]) {
                        addClass("btn", "btn--primary", "consume__btn")
                        graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                        enableWhen { viewModel.canGoNextChapterProperty }
                        tooltip(text)

                        setOnAction {
                            FX.eventbus.fire(GoToNextChapterEvent())
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        logger.info("Final Review docked.")
        timer = startAnimationTimer { viewModel.calculatePosition() }
        viewModel.audioController = AudioPlayerController(scrollbarSlider)
        viewModel.dock()
        subscribeEvents()
    }

    override fun onUndock() {
        logger.info("Final Review undocked.")
        timer?.stop()
        viewModel.undock()
        cleanUpWaveform()
        unsubscribeEvents()
    }

    private fun subscribeEvents() {
        subscribe<MarkerDeletedEvent> {
            viewModel.deleteMarker(it.markerId)
        }.also { eventSubscriptions.add(it) }

        subscribe<MarkerMovedEvent> {
            viewModel.moveMarker(it.markerId, it.start, it.end)
        }.also { eventSubscriptions.add(it) }

        subscribe<UndoChunkingPageEvent> {
            viewModel.undoMarker()
        }.also { eventSubscriptions.add(it) }

        subscribe<RedoChunkingPageEvent> {
            viewModel.redoMarker()
        }.also { eventSubscriptions.add(it) }
    }

    private fun unsubscribeEvents() {
        eventSubscriptions.forEach { it.unsubscribe() }
        eventSubscriptions.clear()
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
            player.bind(viewModel.waveformAudioPlayerProperty)
            secondsToHighlightProperty.set(SECONDS_ON_SCREEN)
        }
    }
}