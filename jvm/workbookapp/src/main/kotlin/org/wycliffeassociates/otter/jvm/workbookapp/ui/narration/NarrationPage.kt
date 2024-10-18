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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.toLazyBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.dialog.ImportAudioDialog
import org.wycliffeassociates.otter.jvm.controls.dialog.LoadingModal
import org.wycliffeassociates.otter.jvm.controls.dialog.PluginOpenedPage
import org.wycliffeassociates.otter.jvm.controls.event.*
import org.wycliffeassociates.otter.jvm.controls.event.BeginRecordingEvent
import org.wycliffeassociates.otter.jvm.controls.event.NextVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.NavigateChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.OpenInAudioPluginEvent
import org.wycliffeassociates.otter.jvm.controls.event.PauseEvent
import org.wycliffeassociates.otter.jvm.controls.event.PauseRecordAgainEvent
import org.wycliffeassociates.otter.jvm.controls.event.PauseRecordingEvent
import org.wycliffeassociates.otter.jvm.controls.event.PlayChapterEvent
import org.wycliffeassociates.otter.jvm.controls.event.PlayVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordAgainEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.ResumeRecordingAgainEvent
import org.wycliffeassociates.otter.jvm.controls.event.ResumeRecordingEvent
import org.wycliffeassociates.otter.jvm.controls.event.SaveRecordingEvent
import org.wycliffeassociates.otter.jvm.controls.model.NotificationStatusType
import org.wycliffeassociates.otter.jvm.controls.model.NotificationViewData
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.NarrationMarkerChangedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.NarrationMovingMarkerEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationOpenImportAudioDialogEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationRedoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationRestartChapterEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationUndoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ImportAudioViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NarrationPage : View() {
    private val logger = LoggerFactory.getLogger(NarrationPage::class.java)

    private val viewModel: NarrationViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val importAudioViewModel: ImportAudioViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()

    private val pluginOpenedPage: PluginOpenedPage

    private val eventSubscriptions = mutableListOf<EventRegistration>()
    private val disposableListeners = mutableListOf<ListenerDisposer>()
    private val disposable = CompositeDisposable()

    private lateinit var narrationHeader: NarrationHeader
    private lateinit var audioWorkspaceView: AudioWorkspaceView
    private lateinit var narrationToolbar: NarrationToolBar
    private lateinit var teleprompterView: TeleprompterView

    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])
        tryImportStylesheet("/css/chapter-grid.css")
        tryImportStylesheet("/css/add-plugin-dialog.css")

        pluginOpenedPage = createPluginOpenedPage()
    }

    override val root = stackpane {
        val narrationRoot = this

        createSnackBar()

        vbox {
            add<NarrationHeader>() {
                narrationHeader = this
            }
            add<AudioWorkspaceView> {
                audioWorkspaceView = this
                this.root.maxHeightProperty().bind(narrationRoot.heightProperty().multiply(1.0 / 3.0))
                this.root.minHeightProperty().bind(this.root.maxHeightProperty())
            }
            add<NarrationToolBar>() {
                narrationToolbar = this
            }
            add<TeleprompterView>() {
                teleprompterView = this
            }
        }
    }

    override fun onDock() {
        super.onDock()
        subscribeToEvents()
        setUpLoadingModal()
        // avoid resetting ViewModel states & action history when coming back from plugin
        when (viewModel.pluginOpenedProperty.value) {
            true -> { // navigate back from plugin
                viewModel.pluginOpenedProperty.set(false)
            }

            false -> { // regular navigation
                viewModel.onDock()
                viewModel.colorThemeProperty.bind(settingsViewModel.appColorMode)
                narrationHeader.onDock()
                audioWorkspaceView.onDock()
                teleprompterView.onDock()
            }
        }
    }

    override fun onUndock() {
        super.onUndock()
        unsubscribeFromEvents()
        disposableListeners.forEach { it.dispose() }
        disposable.clear()
        // avoid resetting ViewModel states & action history when opening plugin
        when (viewModel.pluginOpenedProperty.value) {
            true -> {
                /* no-op, opening plugin */
            }

            false -> { // regular navigation
                viewModel.onUndock()
                narrationHeader.onUndock()
                audioWorkspaceView.onUndock()
                teleprompterView.onUndock()
            }
        }
    }

    private fun subscribeToEvents() {
        addShortcut()

        subscribe<PluginOpenedEvent> { pluginInfo ->
            if (!pluginInfo.isNative) {
                workspace.dock(pluginOpenedPage)
            }
        }.let { eventSubscriptions.add(it) }

        subscribe<SnackBarEvent> {
            viewModel.snackBarMessage(it.message)
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationRestartChapterEvent> {
            viewModel.restartChapter()
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationOpenImportAudioDialogEvent> {
            openImportAudioModal(it.verseIndex)
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationUndoEvent> {
            viewModel.undo()
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationRedoEvent> {
            viewModel.redo()
        }.let { eventSubscriptions.add(it) }

        subscribe<GenerateVerseEvent> {
            /** GENERATE AUDIO FROM TTS */
            viewModel.generateVerseAudio(it.index, it.text)
        }.let {eventSubscriptions.add(it)}

        subscribe<RecordVerseEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<BeginRecordingEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<PauseRecordingEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<PauseRecordAgainEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<ResumeRecordingEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<ResumeRecordingAgainEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationMarkerChangedEvent> {
            logger.info("Received Narration Moved event")
            viewModel.finishMoveMarker(it.index, it.delta)
        }.let { eventSubscriptions.add(it) }

        subscribe<NarrationMovingMarkerEvent> {
            logger.info("Received moving marker event")
            viewModel.startMoveMarker(it.index)
        }.let { eventSubscriptions.add(it) }

        subscribe<NextVerseEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<PlayVerseEvent> {
            viewModel.play(it.index)
        }.let { eventSubscriptions.add(it) }

        subscribe<PlayChapterEvent> {
            viewModel.playAll()
        }.let { eventSubscriptions.add(it) }

        subscribe<PauseEvent> {
            viewModel.pausePlayback()
        }.let { eventSubscriptions.add(it) }

        subscribe<RecordAgainEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<SaveRecordingEvent> {
            viewModel.handleEvent(it)
        }.let { eventSubscriptions.add(it) }

        subscribe<OpenInAudioPluginEvent> {
            viewModel.openInAudioPlugin(it.index)
        }.let { eventSubscriptions.add(it) }

        subscribe<NavigateChapterEvent> {
            viewModel.deferNavigateChapterWhileModifyingTake(it.chapterNumber)
        }.let { eventSubscriptions.add(it) }

        subscribe<ImportVerseAudioEvent> {
            viewModel.importVerseAudio(it.index, it.file)
        }.let { eventSubscriptions.add(it) }

        subscribe<ImportChapterAudioEvent> {
            viewModel.onImportChapterAudio(it.file)
        }.let { eventSubscriptions.add(it) }
    }

    private fun unsubscribeFromEvents() {
        eventSubscriptions.forEach { it.unsubscribe() }
        eventSubscriptions.clear()
    }

    private fun createSnackBar() {
        viewModel
            .snackBarObservable
            .doOnError { e ->
                logger.error("Error in creating no plugin snackbar", e)
            }
            .subscribe { pluginErrorMessage ->
                val notification = NotificationViewData(
                    title = messages["noPlugins"],
                    message = pluginErrorMessage,
                    statusType = NotificationStatusType.WARNING,
                    actionIcon = MaterialDesign.MDI_PLUS,
                    actionText = messages["addApp"]
                ) {
                    audioPluginViewModel.addPlugin(record = true, edit = false)
                }
                SnackbarHandler.showNotification(notification, root)
            }
    }

    private fun setUpLoadingModal() {
        find<LoadingModal>().apply {
            orientationProperty.set(settingsViewModel.orientationProperty.value)
            themeProperty.bind(settingsViewModel.appColorMode)
            messageProperty.bind(viewModel.loadingModalTextProperty)

            viewModel.openLoadingModalProperty.onChangeWithDisposer {
                it?.let {
                    runLater {
                        if (it) {
                            open()
                        } else {
                            close()
                        }
                    }
                }
            }.apply { disposableListeners.add(this) }
        }
    }

    private fun createPluginOpenedPage(): PluginOpenedPage {
        // Plugin active cover
        return find<PluginOpenedPage>().apply {
            licenseProperty.bind(workbookDataStore.sourceLicenseProperty)
            sourceTextProperty.bind(workbookDataStore.sourceTextBinding())
            sourceContentTitleProperty.bind(workbookDataStore.activeTitleBinding())
            orientationProperty.bind(settingsViewModel.orientationProperty)
            sourceOrientationProperty.bind(settingsViewModel.sourceOrientationProperty)
            openLoadingModalProperty.bind(viewModel.openLoadingModalProperty)
            appColorModeProperty.bind(settingsViewModel.appColorMode)

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
        }
    }

    fun openImportAudioModal(verseIndex: Int? = null) {

        find<ImportAudioDialog>().apply {
            themeProperty.set(settingsViewModel.appColorMode.value)
            orientationProperty.set(settingsViewModel.orientationProperty.value)

            verseIndex?.let {
                verseIndexProperty.set(verseIndex)
            }

            validateFileCallback.set {
                importAudioViewModel.isValidImportFile(it)
            }

            setOnCloseAction {
                close()
            }

            importAudioViewModel.snackBarObservable
                .subscribe { msg ->
                    SnackbarHandler.showNotification(
                        NotificationViewData(
                            title = messages["error"],
                            message = msg,
                            statusType = NotificationStatusType.FAILED
                        ),
                        this.root
                    )
                }.addTo(disposable)

            open()
        }
    }

    private fun addShortcut() {
        workspace.shortcut(Shortcut.RECORD.value, viewModel::generateChapterAudio)
    }
}

class SnackBarEvent(val message: String) : FXEvent()