/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.translation

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.javafx.util.Utils
import io.reactivex.rxkotlin.addTo
import javafx.animation.AnimationTimer
import javafx.geometry.Side
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.createAudioScrollBar
import org.wycliffeassociates.otter.jvm.controls.event.GoToNextChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.MarkerDeletedEvent
import org.wycliffeassociates.otter.jvm.controls.event.MarkerMovedEvent
import org.wycliffeassociates.otter.jvm.controls.event.RedoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.event.UndoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
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
    private val audioScrollBar = createAudioScrollBar(
        viewModel.audioPositionProperty,
        viewModel.totalFramesProperty,
        viewModel.isPlayingProperty,
        viewModel::seek
    )
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
                menuSideProperty.set(Side.BOTTOM)
            }
        }
        center = vbox {
            val container = this
            waveform = MarkerWaveform().apply {
                addClass("waveform--focusable")
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
                setOnSeek(viewModel::seek)
                setOnRewind(viewModel::rewind)
                setOnFastForward(viewModel::fastForward)
                setOnToggleMedia(viewModel::mediaToggle)

                viewModel.subscribeOnWaveformImages = ::subscribeOnWaveformImages
                cleanUpWaveform = ::freeImages

                markers.bind(viewModel.markers) { it }
            }
            add(waveform)
            add(audioScrollBar)

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
        addShortcut()

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
        removeShortcut()
    }

    private fun addShortcut() {
        workspace.shortcut(Shortcut.PLAY_SOURCE.value) {
            viewModel.sourcePlayerProperty.value?.toggle()
        }
        workspace.shortcut(Shortcut.PLAY_TARGET.value, viewModel::mediaToggle)
        workspace.shortcut(Shortcut.ADD_MARKER.value, viewModel::placeMarker)
    }

    private fun removeShortcut() {
        workspace.accelerators.remove(Shortcut.PLAY_SOURCE.value)
        workspace.accelerators.remove(Shortcut.PLAY_TARGET.value)
        workspace.accelerators.remove(Shortcut.ADD_MARKER.value)
    }

    private fun subscribeOnWaveformImages() {
        viewModel.waveform
            .observeOnFx()
            .subscribe {
                waveform.addWaveformImage(it)
            }
            .addTo(viewModel.compositeDisposable)
    }
}