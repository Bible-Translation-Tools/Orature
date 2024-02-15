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
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.createAudioScrollBar
import org.wycliffeassociates.otter.jvm.controls.event.TranslationNavigationEvent
import org.wycliffeassociates.otter.jvm.controls.event.MarkerDeletedEvent
import org.wycliffeassociates.otter.jvm.controls.event.MarkerMovedEvent
import org.wycliffeassociates.otter.jvm.controls.event.RedoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.event.UndoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.startAnimationTimer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class Chunking : View() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val viewModel: ChunkingViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()

    private lateinit var waveform: MarkerWaveform
    private val audioScrollBar = createAudioScrollBar(
        viewModel.audioPositionProperty,
        viewModel.totalFramesProperty,
        viewModel.isPlayingProperty,
        viewModel::seek
    )
    private val eventSubscriptions = mutableListOf<EventRegistration>()

    private var timer: AnimationTimer? = null

    override val root = vbox {
        borderpane {
            vgrow = Priority.ALWAYS

            center = VBox().apply {
                MarkerWaveform().apply {
                    waveform = this
                    addClass("waveform--focusable")
                    vgrow = Priority.ALWAYS
                    clip = Rectangle().apply {
                        widthProperty().bind(this@vbox.widthProperty())
                        heightProperty().bind(this@vbox.heightProperty())
                    }
                    themeProperty.bind(settingsViewModel.appColorMode)
                    positionProperty.bind(viewModel.positionProperty)
                    canMoveMarkerProperty.set(true)
                    canDeleteMarkerProperty.set(true)

                    setUpWaveformActionHandlers()

                    // Marker stuff
                    this.markers.bind(viewModel.markers) { it }
                }
                add(waveform)
                add(audioScrollBar)
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
                                    Tooltip(messages["playSource"])
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
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        logger.info("Chunking docked")
        subscribeEvents()
        timer = startAnimationTimer { viewModel.calculatePosition() }
        waveform.initializeMarkers()
        viewModel.subscribeOnWaveformImagesProperty.set(::subscribeOnWaveformImages)
        viewModel.cleanupWaveformProperty.set(waveform::cleanup)
        viewModel.dock()
    }

    override fun onUndock() {
        super.onUndock()
        logger.info("Chunking undocked")
        timer?.stop()
        unsubscribeEvents()
        viewModel.undock()
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

        subscribe<TranslationNavigationEvent> {
            viewModel.cleanupWaveform()
        }.also { eventSubscriptions.add(it) }
    }

    private fun unsubscribeEvents() {
        eventSubscriptions.forEach { it.unsubscribe() }
        eventSubscriptions.clear()
        removeShortcut()
    }

    private fun subscribeOnWaveformImages() {
        viewModel.waveform
            .observeOnFx()
            .subscribe {
                waveform.addWaveformImage(it)
            }
            .addTo(viewModel.compositeDisposable)
    }


    private fun setUpWaveformActionHandlers() {
        waveform.apply {
            setOnSeek { viewModel.seek(it) }
            setOnSeekNext { viewModel.seekNext() }
            setOnSeekPrevious { viewModel.seekPrevious() }
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
        }
    }

    private fun addShortcut() {
        workspace.shortcut(Shortcut.PLAY_SOURCE.value, viewModel::mediaToggle)
        workspace.shortcut(Shortcut.ADD_MARKER.value, viewModel::placeMarker)
    }

    private fun removeShortcut() {
        workspace.accelerators.remove(Shortcut.PLAY_SOURCE.value)
        workspace.accelerators.remove(Shortcut.ADD_MARKER.value)
    }
}