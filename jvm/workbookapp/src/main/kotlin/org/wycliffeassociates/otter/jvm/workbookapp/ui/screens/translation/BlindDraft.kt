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

import com.github.thomasnield.rxkotlinfx.toLazyBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.TakeSelectionAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChunkTakeCard
import org.wycliffeassociates.otter.jvm.controls.event.ChunkTakeEvent
import org.wycliffeassociates.otter.jvm.controls.event.RedoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.controls.event.TakeAction
import org.wycliffeassociates.otter.jvm.controls.event.UndoChunkingPageEvent
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BlindDraftViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class BlindDraft : View() {
    private val logger = LoggerFactory.getLogger(javaClass)

    val viewModel: BlindDraftViewModel by inject()
    val recorderViewModel: RecorderViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()
    val settingsViewModel: SettingsViewModel by inject()
    val translationViewModel: TranslationViewModel2 by inject()

    private val mainSectionProperty = SimpleObjectProperty<Node>(null)
    private val takesView = buildTakesArea()
    private val recordingView = buildRecordingArea()
    private val hideSourceAudio = mainSectionProperty.booleanBinding { it == recordingView }
    private val eventSubscriptions = mutableListOf<EventRegistration>()
    private val listenerDisposers = mutableListOf<ListenerDisposer>()
    private val pluginOpenedPage = createPluginOpenedPage()

    override val root = borderpane {
        addClass("blind-draft")

        top {
            vbox {
                addClass("blind-draft-section")
                label(viewModel.chunkTitleProperty).addClass("h4", "h4--80")
                simpleaudioplayer {
                    playerProperty.bind(viewModel.sourcePlayerProperty)
                    disableProperty().bind(playerProperty.isNull)
                    enablePlaybackRateProperty.set(true)
                    sideTextProperty.set(messages["sourceAudio"])
                    menuSideProperty.set(Side.BOTTOM)
                }
                visibleWhen { hideSourceAudio.not() }
                managedWhen(visibleProperty())
            }
        }
        centerProperty().bind(mainSectionProperty)
    }

    init {
        tryImportStylesheet("/css/recording-screen.css")
        tryImportStylesheet("/css/popup-menu.css")
    }

    private fun buildTakesArea(): VBox {
        val animationMediator = TakeSelectionAnimationMediator<ChunkTakeCard>()

        return VBox().apply {
            vbox {
                addClass("blind-draft-section", "blind-draft-section--top-indent")
                label(messages["best_take"]).addClass("h5", "h5--60")

                vbox {
                    addClass("take-list")
                    bindChildren(viewModel.selectedTake) { take ->
                        ChunkTakeCard(take).apply {
                            animationMediator.selectedNode = this
                            animationMediatorProperty.set(animationMediator)
                        }
                    }
                }
            }
            vbox {
                addClass("blind-draft-section", "blind-draft-section--top-indent")
                label(messages["available_takes"]).addClass("h5", "h5--60")
                vgrow = Priority.ALWAYS

                scrollpane {
                    vgrow = Priority.ALWAYS
                    isFitToWidth = true
                    hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER

                    vbox {
                        addClass("take-list")
                        animationMediator.nodeList = childrenUnmodifiable
                        bindChildren(viewModel.availableTakes) { take ->
                            ChunkTakeCard(take).apply {
                                animationMediatorProperty.set(animationMediator)
                            }
                        }
                    }

                    runLater { customizeScrollbarSkin() }
                }
            }
            hbox {
                addClass("consume__bottom")
                button(messages["new_recording"]) {
                    addClass("btn", "btn--primary")
                    graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)

                    action {
                        viewModel.onRecordNew {
                            mainSectionProperty.set(recordingView)
                            recorderViewModel.onViewReady(takesView.width.toInt()) // use the width of the existing component
                            recorderViewModel.toggle()
                        }
                    }
                }
            }
        }
    }

    private fun buildRecordingArea(): RecordingSection {
        return RecordingSection().apply {
            recorderViewModel.waveformCanvas = waveformCanvas
            recorderViewModel.volumeCanvas = volumeCanvas
            isRecordingProperty.bind(recorderViewModel.recordingProperty)

            setToggleRecordingAction {
                recorderViewModel.toggle()
            }

            setCancelAction {
                recorderViewModel.cancel()
                viewModel.onRecordFinish(RecorderViewModel.Result.CANCELLED)
                mainSectionProperty.set(takesView)
            }

            setSaveAction {
                val result = recorderViewModel.saveAndQuit()
                viewModel.onRecordFinish(result)
                mainSectionProperty.set(takesView)
            }
        }
    }

    override fun onDock() {
        super.onDock()
        recorderViewModel.waveformCanvas = recordingView.waveformCanvas
        recorderViewModel.volumeCanvas = recordingView.volumeCanvas
        mainSectionProperty.set(takesView)
        when (viewModel.pluginOpenedProperty.value) {
            true -> {
                // navigate back from plugin
                viewModel.pluginOpenedProperty.set(false)
                translationViewModel.loadingStepProperty.set(false)
            }
            false -> {
                logger.info("Blind Draft docked.")
                viewModel.dockBlindDraft()
            }
        }
        subscribeEvents()
    }

    override fun onUndock() {
        super.onUndock()
        when (viewModel.pluginOpenedProperty.value) {
            true -> {
                /* no-op, opening plugin */
            }
            false -> {
                logger.info("Blind Draft undocked.")
                viewModel.undockBlindDraft()
            }
        }
        unsubscribeEvents()
        if (mainSectionProperty.value == recordingView) {
            recorderViewModel.cancel()
        }
    }

    private fun createPluginOpenedPage(): PluginOpenedPage {
        return find<PluginOpenedPage>().apply {
            licenseProperty.bind(workbookDataStore.sourceLicenseProperty)
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            sourceContentTitleProperty.bind(workbookDataStore.activeTitleBinding())
            orientationProperty.bind(settingsViewModel.orientationProperty)
            sourceOrientationProperty.bind(settingsViewModel.sourceOrientationProperty)

            sourceSpeedRateProperty.bind(
                workbookDataStore.activeWorkbookProperty.select {
                    it.translation.sourceRate.toLazyBinding()
                }
            )

            targetSpeedRateProperty.bind(
                workbookDataStore.activeWorkbookProperty.select {
                    it.translation.targetRate.toLazyBinding()
                }
            )

            playerProperty.bind(viewModel.sourcePlayerProperty)
        }
    }

    private fun subscribeEvents() {
        addShortcut()

        viewModel.currentChunkProperty.onChangeWithDisposer { selectedChunk ->
            // clears recording screen if another chunk is selected
            if (selectedChunk != null && mainSectionProperty.value == recordingView) {
                recorderViewModel.cancel()
                mainSectionProperty.set(takesView)
            }
        }.also { listenerDisposers.add(it) }
        
        subscribe<ChunkTakeEvent> {
            when (it.action) {
                TakeAction.SELECT -> viewModel.onSelectTake(it.take)
                TakeAction.DELETE -> viewModel.onDeleteTake(it.take)
                TakeAction.EDIT -> {
                    // TODO()
                }
            }
        }.also { eventSubscriptions.add(it) }

        subscribe<UndoChunkingPageEvent> {
            viewModel.undo()
        }.also { eventSubscriptions.add(it) }

        subscribe<RedoChunkingPageEvent> {
            viewModel.redo()
        }.also { eventSubscriptions.add(it) }

        subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                workspace.dock(pluginOpenedPage)
            }
        }.let { eventSubscriptions.add(it) }
    }

    private fun unsubscribeEvents() {
        eventSubscriptions.forEach { it.unsubscribe() }
        eventSubscriptions.clear()
        listenerDisposers.forEach { it.dispose() }
        listenerDisposers.clear()
        removeShortcut()
    }

    private fun addShortcut() {
        workspace.shortcut(Shortcut.PLAY_SOURCE.value) {
            viewModel.sourcePlayerProperty.value?.toggle()
        }
    }

    private fun removeShortcut() {
        workspace.accelerators.remove(Shortcut.PLAY_SOURCE.value)
    }
}