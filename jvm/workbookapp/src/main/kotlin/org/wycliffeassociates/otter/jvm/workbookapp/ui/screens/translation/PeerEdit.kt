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
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.createAudioScrollBar
import org.wycliffeassociates.otter.jvm.controls.event.TranslationNavigationEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.event.RedoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.event.UndoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerWaveform
import org.wycliffeassociates.otter.jvm.controls.waveform.startAnimationTimer
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.PeerEditViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

open class PeerEdit : View() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val viewModel: PeerEditViewModel by inject()
    val settingsViewModel: SettingsViewModel by inject()
    val recorderViewModel: RecorderViewModel by inject()

    private lateinit var waveform: MarkerWaveform
    private val audioScrollBar = createAudioScrollBar(
        viewModel.audioPositionProperty,
        viewModel.totalFramesProperty,
        viewModel.isPlayingProperty,
        viewModel::seek
    )
    private var timer: AnimationTimer? = null

    private val mainSectionProperty = SimpleObjectProperty<Node>(null)
    private val playbackView = createPlaybackView()
    private val recordingView = createRecordingView()
    private val eventSubscriptions = mutableListOf<EventRegistration>()
    private val listenerDisposers = mutableListOf<ListenerDisposer>()

    override val root = borderpane {
        top = vbox {
            addClass("blind-draft-section")
            label(viewModel.chunkTitleProperty).addClass("h4", "h4--80")
            simpleaudioplayer {
                playerProperty.bind(viewModel.sourcePlayerProperty)
                disableProperty().bind(playerProperty.isNull)
                enablePlaybackRateProperty.set(true)
                sideTextProperty.set(messages["sourceAudio"])
                menuSideProperty.set(Side.BOTTOM)
            }
        }
        centerProperty().bind(mainSectionProperty)
    }

    init {
        tryImportStylesheet("/css/recording-screen.css")
    }

    private fun createPlaybackView() = VBox().apply {
        val container = this
        waveform = createPlaybackWaveform(container)
        add(waveform)
        add(audioScrollBar)

        hbox {
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
                tooltip {
                    textProperty().bind(this@button.textProperty())
                }

                action {
                    viewModel.toggleAudio()
                }
            }
            button(messages["confirm"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE)
                tooltip(text)

                visibleWhen { viewModel.isPlayingProperty.not() }
                disableWhen { viewModel.chunkConfirmed }

                action {
                    viewModel.confirmChunk()
                }
            }
            region { hgrow = Priority.ALWAYS }
            button(messages["record"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                tooltip(text)

                disableWhen { viewModel.isPlayingProperty }

                action {
                    viewModel.onRecordNew()
                    mainSectionProperty.set(recordingView)
                    recorderViewModel.onViewReady(container.width.toInt()) // use the width of the existing component
                    recorderViewModel.toggle()
                }
            }
        }
    }

    private fun createPlaybackWaveform(container: VBox): MarkerWaveform {
        return MarkerWaveform().apply {
            addClass("waveform--focusable")
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
            setOnRewind(viewModel::rewind)
            setOnFastForward(viewModel::fastForward)
            setOnToggleMedia(viewModel::toggleAudio)

            minWidth = 0.0
        }
    }

    private fun createRecordingView(): RecordingSection {
        return RecordingSection().apply {
            isRecordingProperty.bind(recorderViewModel.recordingProperty)

            setToggleRecordingAction {
                recorderViewModel.toggle()
            }

            setCancelAction {
                recorderViewModel.cancel()
                viewModel.onRecordFinish(RecorderViewModel.Result.CANCELLED)
                mainSectionProperty.set(playbackView)
            }

            setSaveAction {
                val result = recorderViewModel.saveAndQuit()
                viewModel.onRecordFinish(result)
                mainSectionProperty.set(playbackView)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        logger.info("Checking docked.")
        recorderViewModel.waveformCanvas = recordingView.waveformCanvas
        recorderViewModel.volumeCanvas = recordingView.volumeCanvas
        mainSectionProperty.set(playbackView)
        timer = startAnimationTimer { viewModel.calculatePosition() }
        viewModel.subscribeOnWaveformImages = ::subscribeOnWaveformImages
        viewModel.cleanUpWaveform = waveform::cleanup
        viewModel.dock()
        subscribeEvents()
    }

    override fun onUndock() {
        super.onUndock()
        logger.info("Checking undocked.")
        timer?.stop()
        unsubscribeEvents()
        viewModel.undock()
        if (mainSectionProperty.value == recordingView) {
            recorderViewModel.cancel()
        }
    }

    private fun subscribeEvents() {
        addShortcut()

        viewModel.currentChunkProperty.onChangeWithDisposer { selectedChunk ->
            // clears recording screen if another chunk is selected
            if (selectedChunk != null && mainSectionProperty.value == recordingView) {
                recorderViewModel.cancel()
                mainSectionProperty.set(playbackView)
            }
        }.also { listenerDisposers.add(it) }

        subscribe<UndoChunkingPageEvent> {
            viewModel.undo()
        }.also { eventSubscriptions.add(it) }

        subscribe<RedoChunkingPageEvent> {
            viewModel.redo()
        }.also { eventSubscriptions.add(it) }

        subscribe<TranslationNavigationEvent> {
            waveform.cleanup()
        }.also { eventSubscriptions.add(it) }

        subscribe<NavigationRequestEvent> { // navigate Home
            waveform.cleanup()
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
            .addTo(viewModel.disposable)
    }

    private fun addShortcut() {
        workspace.shortcut(Shortcut.PLAY_SOURCE.value) {
            viewModel.sourcePlayerProperty.value?.toggle()
        }
        workspace.shortcut(Shortcut.PLAY_TARGET.value, viewModel::toggleAudio)
    }

    private fun removeShortcut() {
        workspace.accelerators.remove(Shortcut.PLAY_SOURCE.value)
        workspace.accelerators.remove(Shortcut.PLAY_TARGET.value)
    }
}