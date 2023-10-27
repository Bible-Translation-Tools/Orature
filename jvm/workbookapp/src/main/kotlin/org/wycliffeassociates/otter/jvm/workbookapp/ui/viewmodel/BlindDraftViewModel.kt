package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.transformation.FilteredList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.IUndoable
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.common.domain.chunking.ChunkTakeDeleteAction
import org.wycliffeassociates.otter.common.domain.chunking.ChunkTakeRecordAction
import org.wycliffeassociates.otter.common.domain.chunking.ChunkTakeSelectAction
import org.wycliffeassociates.otter.common.domain.chunking.ChunkingHistory
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel.Result
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.util.*
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

    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val currentChunkProperty = SimpleObjectProperty<Chunk>()
    val chunkTitleProperty = workbookDataStore.activeChunkTitleBinding()
    val takes = observableListOf<TakeCardModel>()
    val selectedTake = FilteredList<TakeCardModel>(takes) { it.selected }
    val availableTakes = FilteredList<TakeCardModel>(takes) { !it.selected }

    private val recordedTakeProperty = SimpleObjectProperty<Take>()
    private val actionHistory = ChunkingHistory()

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
            }
            actionHistory.clear()
        }.also { disposableListeners.add(it) }
    }

    fun undockBlindDraft() {
        sourcePlayerProperty.unbind()
        currentChunkProperty.set(null)
        translationViewModel.updateSourceText()
        selectedTakeDisposable.clear()
        disposables.clear()
        disposableListeners.forEach { it.dispose() }
        disposableListeners.clear()
    }

    fun onRecordNew() {
        newTakeFile()
            .observeOnFx()
            .subscribe { take ->
                recordedTakeProperty.set(take)
                recorderViewModel.targetFileProperty.set(take.file)
            }
    }

    fun onRecordFinish(result: Result) {
        if (result == Result.SUCCESS) {
            workbookDataStore.chunk?.let { chunk ->
                val op = ChunkTakeRecordAction(
                    recordedTakeProperty.value,
                    chunk,
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
            val op = ChunkTakeSelectAction(take, chunk, selectedTake)
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
            val op = ChunkTakeDeleteAction(
                take,
                chunk,
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
            .chunks
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
            chapter.chunks.value?.let { chunks ->
                translationViewModel.loadChunks(chunks)
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