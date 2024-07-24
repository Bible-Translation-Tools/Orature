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
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.transformation.FilteredList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.IUndoable
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.common.domain.translation.TranslationTakeDeleteAction
import org.wycliffeassociates.otter.common.domain.translation.TranslationTakeRecordAction
import org.wycliffeassociates.otter.common.domain.translation.TranslationTakeSelectAction
import org.wycliffeassociates.otter.common.domain.model.UndoableActionHistory
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPlugin
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.SnackBarEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel.Result
import tornadofx.*
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class BlindDraftViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var waveFileCreator: IWaveFileCreator

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()
    val recorderViewModel: RecorderViewModel by inject()
    val chapterReviewViewModel: ChapterReviewViewModel by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val currentChunkProperty = SimpleObjectProperty<Chunk>()
    val chunkTitleProperty = workbookDataStore.activeChunkTitleBinding()
    val takes = observableListOf<TakeCardModel>()
    val selectedTake = FilteredList<TakeCardModel>(takes) { it.selected }
    val availableTakes = FilteredList<TakeCardModel>(takes) { !it.selected }
    val pluginOpenedProperty = SimpleBooleanProperty(false)

    private val recordedTakeProperty = SimpleObjectProperty<Take>()
    private val actionHistory = UndoableActionHistory<IUndoable>()

    private val selectedTakeDisposable = CompositeDisposable()
    private val disposables = CompositeDisposable()
    private val disposableListeners = mutableListOf<ListenerDisposer>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        currentChunkProperty.bindBidirectional(workbookDataStore.activeChunkProperty)
    }

    fun dockBlindDraft() {
        subscribeToChunks()

        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        currentChunkProperty.onChangeAndDoNowWithDisposer {
            it?.let { chunk ->
                subscribeSelectedTakePropertyToRelay(chunk)
                if (actionHistory.canUndo()) {
                    chapterReviewViewModel.invalidateChapterTake() // resets chapter target audio when changes detected
                }
            }
            actionHistory.clear()
        }.also { disposableListeners.add(it) }

        translationViewModel.pluginOpenedProperty.bind(pluginOpenedProperty)
        translationViewModel.loadingStepProperty.set(false)
    }

    fun undockBlindDraft() {
        workbookDataStore.workbook.let { wb ->
            wb.projectFilesAccessor.updateSelectedTakesFile(wb).subscribe()
        }
        audioDataStore.stopPlayers()
        audioDataStore.closePlayers()
        audioConnectionFactory.releasePlayer()
        if (actionHistory.canUndo()) {
            chapterReviewViewModel.invalidateChapterTake()
            actionHistory.clear()
        }
        sourcePlayerProperty.unbind()
        currentChunkProperty.set(null)
        translationViewModel.pluginOpenedProperty.unbind()
        translationViewModel.updateSourceText().subscribe()
        selectedTakeDisposable.clear()
        disposables.clear()
        disposableListeners.forEach { it.dispose() }
        disposableListeners.clear()
    }

    fun onRecordNew(toggleViewCallback: () -> Unit = {}) {
        val pluginType = PluginType.RECORDER
        val selectedPlugin = audioPluginViewModel.getPlugin(pluginType)
            .blockingGet()
        if (!selectedPlugin.isNativePlugin()) {
            recordWithExternalPlugin(selectedPlugin, pluginType)
        } else {
            newTakeFile()
                .observeOnFx()
                .subscribe { take ->
                    recordedTakeProperty.set(take)
                    recorderViewModel.targetFileProperty.set(take.file)
                }
            toggleViewCallback()
        }

    }

    fun onRecordFinish(result: Result) {
        if (result == Result.SUCCESS) {
            workbookDataStore.chunk?.let { chunk ->
                val op = TranslationTakeRecordAction(
                    chunk,
                    recordedTakeProperty.value,
                    chunk.audio.getSelectedTake()
                )
                actionHistory.execute(op)
                onUndoableAction()
                loadTakes(chunk)
            }
        } else {
            recordedTakeProperty.value?.file?.delete()
            recordedTakeProperty.set(null)
        }
    }

    fun onSelectTake(take: Take) {
        currentChunkProperty.value?.let { chunk ->
            take.file.setLastModified(System.currentTimeMillis())
            val selectedTake = chunk.audio.getSelectedTake()
            val op = TranslationTakeSelectAction(chunk, take, selectedTake)
            actionHistory.execute(op)
            onUndoableAction()
        }
    }

    private fun selectTake(take: Take) {
        currentChunkProperty.value?.audio?.selectTake(take)
    }

    fun onDeleteTake(take: Take) {
        takes.forEach { it.audioPlayer.stop() }
        audioDataStore.stopPlayers()

        currentChunkProperty.value?.let { chunk ->
            val op = TranslationTakeDeleteAction(
                chunk,
                take,
                takes.any { it.take == take && it.selected },
                ::handlePostDeleteTake
            )
            actionHistory.execute(op)
            onUndoableAction()
        }
    }

    fun undo() {
        if (!actionHistory.canUndo()) {
            translationViewModel.canUndoProperty.set(false)
            return
        }

        takes.forEach { it.audioPlayer.stop() }
        audioDataStore.stopPlayers()
        actionHistory.undo()
        currentChunkProperty.value?.let { loadTakes(it) }
        translationViewModel.canUndoProperty.set(actionHistory.canUndo())
        translationViewModel.canRedoProperty.set(true)
    }

    fun redo() {
        if (!actionHistory.canRedo()) {
            translationViewModel.canRedoProperty.set(false)
            return
        }

        takes.forEach { it.audioPlayer.stop() }
        audioDataStore.stopPlayers()
        actionHistory.redo()
        currentChunkProperty.value?.let { loadTakes(it) }
        translationViewModel.canRedoProperty.set(actionHistory.canRedo())
        translationViewModel.canUndoProperty.set(true)
    }

    private fun subscribeToChunks() {
        val chapter = workbookDataStore.chapter
        chapter
            .observableChunks
            .map { contents ->
                contents.filter { it.contentType == ContentType.TEXT } // omit titles
            }
            .observeOnFx()
            .subscribe { chunks ->
                translationViewModel.loadChunks(chunks)
                (chunks.firstOrNull { !it.hasSelectedAudio() } ?: chunks.firstOrNull())
                    ?.let { chunk ->
                        translationViewModel.selectChunk(chunk.sort)
                    }
            }.addTo(disposables)
    }

    private fun loadTakes(chunk: Chunk) {
        val selected = chunk.audio.selected.value?.value

        val takeList = chunk.audio.getAllTakes()
            .filter { !it.isDeleted() }
            .map { take ->
                take.mapToCardModel(take == selected)
            }
            .sortedByDescending { it.take.file.lastModified() }

        takes.setAll(takeList)
    }

    private fun subscribeSelectedTakePropertyToRelay(chunk: Chunk) {
        selectedTakeDisposable.clear()
        chunk.audio.selected
            .observeOnFx()
            .subscribe {
                refreshChunkList()
                loadTakes(chunk)
            }.addTo(selectedTakeDisposable)
    }

    private fun refreshChunkList() {
        workbookDataStore.activeChapterProperty.value?.let { chapter ->
            chapter.chunks.blockingGet().let { chunks ->
                translationViewModel.loadChunks(
                    chunks.filter { it.contentType == ContentType.TEXT }
                )
            }
        }
    }

    fun newTakeFile(): Single<Take> {
        return workbookDataStore.chunk!!.let { chunk ->
            val namer = getFileNamer(chunk)
            val chapter = namer.formatChapterNumber()
            val chapterAudioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir
                .resolve(chapter)
                .apply { mkdirs() }

            chunk.audio.getNewTakeNumber()
                .map { takeNumber ->
                    createNewTake(
                        takeNumber,
                        namer.generateName(takeNumber, AudioFileFormat.WAV),
                        chapterAudioDir,
                        true
                    )
                }
        }
    }

    private fun createNewTake(
        newTakeNumber: Int,
        filename: String,
        audioDir: File,
        createEmpty: Boolean
    ): Take {
        val takeFile = audioDir.resolve(File(filename))
        val newTake = Take(
            name = takeFile.name,
            file = takeFile,
            number = newTakeNumber,
            format = MimeType.WAV,
            createdTimestamp = LocalDate.now()
        )
        if (createEmpty) {
            newTake.file.createNewFile()
            waveFileCreator.createEmpty(newTake.file)
        }
        return newTake
    }

    private fun getFileNamer(recordable: Recordable): FileNamer {
        return WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbookDataStore.workbook,
            chapter = workbookDataStore.chapter,
            chunk = workbookDataStore.chunk,
            recordable = recordable,
            rcSlug = workbookDataStore.workbook.sourceMetadataSlug
        )
    }

    private fun handlePostDeleteTake(take: Take, selectAnotherTake: Boolean) {
        Platform.runLater {
            takes.removeIf { it.take == take }
            // select the next take after deleting
            if (selectAnotherTake) {
                takes.firstOrNull()?.let {
                    selectTake(it.take)
                }
            }
        }
    }

    private fun recordWithExternalPlugin(plugin: IAudioPlugin, pluginType: PluginType) {
        pluginOpenedProperty.set(true)
        workbookDataStore.activeTakeNumberProperty.set(1)
        FX.eventbus.fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
        newTakeFile()
            .flatMap { take ->
                recordedTakeProperty.set(take)
                audioPluginViewModel.edit(take.file)
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType", e)
            }
            .onErrorReturn { PluginActions.Result.NO_PLUGIN }
            .subscribe { result ->
                logger.info("Returned from plugin with result: $result")

                when (result) {
                    PluginActions.Result.NO_PLUGIN -> {
                        FX.eventbus.fire(SnackBarEvent(messages["noEditor"]))
                    }

                    PluginActions.Result.SUCCESS -> {
                        // handle nonempty take returned from plugin
                        val file = recordedTakeProperty.value.file
                        if (AudioFile(file).totalFrames > 0) {
                            workbookDataStore.chunk?.let { chunk ->
                                val op = TranslationTakeRecordAction(
                                    chunk,
                                    recordedTakeProperty.value,
                                    chunk.audio.getSelectedTake()
                                )
                                actionHistory.execute(op)
                                onUndoableAction()
                                loadTakes(chunk)
                            }
                        }
                    }

                    else -> {
                        // no audio - no op
                    }
                }
                recordedTakeProperty.set(null)
                FX.eventbus.fire(PluginClosedEvent(pluginType))
            }
    }

    private fun onUndoableAction() {
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(false)
    }

    fun Take.mapToCardModel(selected: Boolean): TakeCardModel {
        val audioPlayer: IAudioPlayer = audioConnectionFactory.getPlayer()
        audioPlayer.load(this.file)
        return TakeCardModel(this, selected, audioPlayer)
    }
}