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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.getWaveformColors
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.IUndoable
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.translation.TranslationTakeApproveAction
import org.wycliffeassociates.otter.common.domain.model.UndoableActionHistory
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPlugin
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.ScrollSpeed
import org.wycliffeassociates.otter.jvm.controls.waveform.IWaveformViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import org.wycliffeassociates.otter.jvm.controls.waveform.WAVEFORM_MAX_HEIGHT
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import tornadofx.*
import javax.inject.Inject

class PeerEditViewModel : ViewModel(), IWaveformViewModel {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    val settingsViewModel: SettingsViewModel by inject()
    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()
    val blindDraftViewModel: BlindDraftViewModel by inject()
    val recorderViewModel: RecorderViewModel by inject()
    val chapterReviewViewModel: ChapterReviewViewModel by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty()
    override val audioPositionProperty = SimpleIntegerProperty()

    val chunkTitleProperty = workbookDataStore.activeChunkTitleBinding()
    val currentChunkProperty = SimpleObjectProperty<Chunk>()
    val chunkConfirmed = SimpleBooleanProperty(false)
    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val isPlayingProperty = SimpleBooleanProperty(false)
    val pluginOpenedProperty = SimpleBooleanProperty(false)
    val disposable = CompositeDisposable()

    lateinit var waveform: Observable<Image>
    var subscribeOnWaveformImagesProperty = SimpleObjectProperty {}
    val cleanupWaveformProperty = SimpleObjectProperty {}
    private var audioController: AudioPlayerController? = null

    override var sampleRate: Int = 0 // beware of divided by 0
    override val totalFramesProperty = SimpleIntegerProperty(0)
    override var totalFrames: Int by totalFramesProperty // beware of divided by 0

    private val newTakeProperty = SimpleObjectProperty<Take>(null)
    private val currentStep: ChunkingStep by translationViewModel.selectedStepProperty
    private val builder = ObservableWaveformBuilder()
    private val height = Integer.min(Screen.getMainScreen().platformHeight, WAVEFORM_MAX_HEIGHT.toInt())
    private val width = Screen.getMainScreen().platformWidth
    private val disposableListeners = mutableListOf<ListenerDisposer>()
    private val selectedTakeDisposable = CompositeDisposable()

    private val actionHistory = UndoableActionHistory<IUndoable>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        currentChunkProperty.bindBidirectional(workbookDataStore.activeChunkProperty)
    }

    fun dock() {
        subscribeToChunks()
        currentChunkProperty.onChangeAndDoNowWithDisposer {
            it?.let { chunk ->
                subscribeToSelectedTake(chunk)
                val isConfirmed = chunk.checkingStatus().ordinal >= checkingStatusFromStep(currentStep).ordinal
                chunkConfirmed.set(isConfirmed)
            }
            actionHistory.clear()
        }.also { disposableListeners.add(it) }

        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        translationViewModel.pluginOpenedProperty.bind(pluginOpenedProperty)
        translationViewModel.loadingStepProperty.set(false)
    }

    fun undock() {
        audioDataStore.stopPlayers()
        audioDataStore.closePlayers()
        waveformAudioPlayerProperty.value?.stop()
        waveformAudioPlayerProperty.value?.close()
        sourcePlayerProperty.unbind()
        currentChunkProperty.set(null)
        selectedTakeDisposable.clear()
        translationViewModel.pluginOpenedProperty.unbind()
        disposable.clear()
        disposableListeners.forEach { it.dispose() }
        disposableListeners.clear()
        actionHistory.clear()
        cleanupWaveform()
    }

    fun refreshChunkList() {
        workbookDataStore.activeChapterProperty.value?.let { chapter ->
            chapter.chunks.blockingGet().let { chunks ->
                translationViewModel.loadChunks(
                    chunks.filter { it.contentType == ContentType.TEXT }
                )
            }
        }
    }

    fun toggleAudio() {
        audioController?.toggle()
    }

    fun pause() {
        audioController?.pause()
    }

    fun seek(location: Int) {
        audioController?.seek(location)
    }

    fun rewind(speed: ScrollSpeed) {
        if (!isPlayingProperty.value) {
            audioController?.rewind(speed)
        }
    }

    fun fastForward(speed: ScrollSpeed) {
        if (!isPlayingProperty.value) {
            audioController?.fastForward(speed)
        }
    }

    fun confirmChunk() {
        currentChunkProperty.value?.let { chunk ->
            chunkConfirmed.set(true)
            val checkingStatus = checkingStatusFromStep(currentStep)
            val take = chunk.audio.getSelectedTake()!!
            take.checkingState
                .take(1)
                .observeOnFx()
                .subscribe { currentChecking ->
                    val op = TranslationTakeApproveAction(
                        take,
                        checkingStatus,
                        currentChecking
                    ).apply {
                        setUndoCallback { chunkConfirmed.set(false) }
                        setRedoCallback { chunkConfirmed.set(true) }
                    }
                    actionHistory.execute(op)
                    onUndoableAction()
                    refreshChunkList()
                }.dispose()
        }
    }

    fun undo() {
        actionHistory.undo()
        refreshChunkList()
        translationViewModel.canRedoProperty.set(true)
        translationViewModel.canUndoProperty.set(actionHistory.canUndo())
    }

    fun redo() {
        actionHistory.redo()
        refreshChunkList()
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(actionHistory.canRedo())
    }

    fun onRecordNew(toggleViewCallback: () -> Unit = {}) {
        val pluginType = PluginType.RECORDER
        val selectedPlugin = audioPluginViewModel.getPlugin(pluginType)
            .blockingGet()
        if (!selectedPlugin.isNativePlugin()) {
            recordWithExternalPlugin(selectedPlugin, pluginType)
        } else {
            val chunk = workbookDataStore.chunk!!
            recorderViewModel.createTake(chunk, chunk, createEmpty = true)
                .observeOnFx()
                .subscribe { take ->
                    newTakeProperty.set(take)
                    recorderViewModel.targetFileProperty.set(take.file)
                }

            toggleViewCallback()
        }
    }

    fun onRecordFinish(result: RecorderViewModel.Result) {
        if (result == RecorderViewModel.Result.SUCCESS) {
            workbookDataStore.chunk?.audio?.insertTake(newTakeProperty.value)
            chapterReviewViewModel.invalidateChapterTake()
            // any change(s) to chunk's take requires checking again
            translationViewModel.selectedStepProperty.set(null)
            translationViewModel.navigateStep(ChunkingStep.PEER_EDIT)
        } else {
            newTakeProperty.value?.file?.delete()
            newTakeProperty.set(null)
        }
    }

    fun cleanupWaveform() {
        cleanupWaveformProperty.value.invoke()
    }

    fun subscribeOnWaveformImages() {
        subscribeOnWaveformImagesProperty.value.invoke()
    }

    private fun subscribeToChunks() {
        workbookDataStore.chapter
            .observableChunks
            .map { contents ->
                contents.filter { it.contentType == ContentType.TEXT } // omit titles
            }
            .observeOnFx()
            .subscribe { chunks ->
                translationViewModel.loadChunks(chunks)

                val chunkToSelect = chunks.firstOrNull { c ->
                    c.checkingStatus().ordinal < checkingStatusFromStep(currentStep).ordinal
                } ?: chunks.firstOrNull()
                chunkToSelect?.let { chunk ->
                    translationViewModel.selectChunk(chunk.sort)
                }
            }.addTo(disposable)
    }

    fun onThemeChange() {

        // Avoids null error in createWaveformImages cause by player not yet being initialized.
        val hasAudioAndPlayer =
            waveformAudioPlayerProperty.value != null && waveformAudioPlayerProperty.value.getDurationInFrames() > 0

        if (!hasAudioAndPlayer) {
            return
        }

        val take = currentChunkProperty.value?.audio?.getSelectedTake()
        take?.let {
            pause()
            builder.cancel()
            cleanupWaveform()

            val audio = OratureAudioFile(take.file)
            createWaveformImages(audio)
            subscribeOnWaveformImages()
        }
    }

    private fun subscribeToSelectedTake(chunk: Chunk) {
        selectedTakeDisposable.clear()
        chunk.audio.selected
            .observeOnFx()
            .subscribe {
                it.value?.let { take -> loadTargetAudio(take) }
            }.addTo(selectedTakeDisposable)
    }

    private fun loadTargetAudio(take: Take) {
        val audioPlayer: IAudioPlayer = audioConnectionFactory.getPlayer()
        audioPlayer.load(take.file)
        audioPlayer.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
        }
        waveformAudioPlayerProperty.set(audioPlayer)
        audioController = AudioPlayerController().also { controller ->
            controller.load(audioPlayer)
            isPlayingProperty.bind(controller.isPlayingProperty)
        }

        val audio = OratureAudioFile(take.file)
        createWaveformImages(audio)
        subscribeOnWaveformImages()
    }
    private fun recordWithExternalPlugin(plugin: IAudioPlugin, pluginType: PluginType) {
        pluginOpenedProperty.set(true)
        workbookDataStore.activeTakeNumberProperty.set(1)
        FX.eventbus.fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))

        val chunk = workbookDataStore.chunk!!
        recorderViewModel.createTake(chunk, chunk, createEmpty = true)
            .flatMap { take ->
                newTakeProperty.set(take)
                // doesn't need to create take since .record() will do
                audioPluginViewModel.record(take)
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType", e)
            }
            .onErrorReturn { PluginActions.Result.NO_PLUGIN }
            .subscribe { result ->
                logger.info("Returned from plugin with result: $result")

                val take = newTakeProperty.value
                if (AudioFile(take.file).totalFrames > 0) {
                    /* set pluginOpenedProperty to false will allow invoking dock(),
                    which updates chunk status and auto navigates to incomplete chunk.
                    This only applies to non-empty recording. */
                    pluginOpenedProperty.set(false)
                    currentChunkProperty.value.audio.insertTake(take)
                    chapterReviewViewModel.invalidateChapterTake()
                    // any change(s) to chunk's take requires checking again
                    translationViewModel.selectedStepProperty.set(null)
                    translationViewModel.navigateStep(ChunkingStep.PEER_EDIT)
                }

                newTakeProperty.set(null)
                FX.eventbus.fire(PluginClosedEvent(pluginType))
            }
    }

    private fun createWaveformImages(audio: OratureAudioFile) {
        cleanupWaveform()
        imageWidthProperty.set(computeImageWidth(width))

        val waveformColors = getWaveformColors(settingsViewModel.appColorMode.value)

        waveformColors.let {
            builder.cancel()
            waveform = builder.buildAsync(
                audio.reader(),
                width = imageWidthProperty.value.toInt(),
                height = height,
                wavColor = Color.web(waveformColors.wavColorHex),
                background = Color.web(waveformColors.backgroundColorHex)
            )
        }
    }

    private fun onUndoableAction() {
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(false)
    }

    private fun checkingStatusFromStep(step: ChunkingStep): CheckingStatus {
        return when (step) {
            ChunkingStep.PEER_EDIT -> CheckingStatus.PEER_EDIT
            ChunkingStep.KEYWORD_CHECK -> CheckingStatus.KEYWORD
            ChunkingStep.VERSE_CHECK -> CheckingStatus.VERSE
            else -> CheckingStatus.UNCHECKED
        }
    }
}

