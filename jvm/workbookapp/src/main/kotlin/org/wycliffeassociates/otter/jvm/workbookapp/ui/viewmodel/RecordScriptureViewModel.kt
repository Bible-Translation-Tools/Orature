/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.ButtonType
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.common.utils.capitalizeString
import org.wycliffeassociates.otter.jvm.controls.ListAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.card.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.TakeEvent
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import io.reactivex.rxkotlin.toObservable as toRxObservable

private const val NO_HIGHLIGHT_INDEX = -1

class RecordScriptureViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(RecordScriptureViewModel::class.java)

    private enum class StepDirection {
        FORWARD,
        BACKWARD
    }

    private val workbookDataStore: WorkbookDataStore by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    // This will be bidirectionally bound to workbookDataStore's activeChapterProperty
    private val activeChapterProperty = SimpleObjectProperty<Chapter>()
    private val activeChapter: Chapter
        get() = activeChapterProperty.value ?: throw IllegalStateException("Chapter is null")

    // This will be bidirectionally bound to workbookDataStore's activeChunkProperty
    private val activeChunkProperty = SimpleObjectProperty<Chunk>()
    private val activeChunk: Chunk
        get() = activeChunkProperty.value ?: throw IllegalStateException("Chunk is null")

    private val titleProperty = SimpleStringProperty()
    private var title by titleProperty

    private val chapterList: ObservableList<Chapter> = observableListOf()
    val hasNextChapter = SimpleBooleanProperty(false)
    val hasPreviousChapter = SimpleBooleanProperty(false)

    private val chunkList: ObservableList<Chunk> = observableListOf()
    val hasNextChunk = SimpleBooleanProperty(false)
    val hasPreviousChunk = SimpleBooleanProperty(false)

    val isChunk = activeChunkProperty.isNotNull
    val highlightedChunkProperty = SimpleIntegerProperty(NO_HIGHLIGHT_INDEX)
    val verseCountProperty = SimpleIntegerProperty()

    private var activeChunkSubscription: Disposable? = null

    val recordableProperty = SimpleObjectProperty<Recordable?>()
    var recordable by recordableProperty

    val contextProperty = SimpleObjectProperty(PluginType.RECORDER)
    val sourceAudioAvailableProperty = workbookDataStore.sourceAudioAvailableProperty
    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer?>(null)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()
    val takeCardModels: ObservableList<TakeCardModel> = FXCollections.observableArrayList()
    val takeCardViews: ObservableList<ScriptureTakeCard> = FXCollections.observableArrayList()

    val showImportProgressDialogProperty = SimpleBooleanProperty(false)
    val showImportSuccessDialogProperty = SimpleBooleanProperty(false)
    val showImportFailDialogProperty = SimpleBooleanProperty(false)

    private val disposables = CompositeDisposable()
    val listeners = mutableListOf<ListenerDisposer>()

    init {
        activeChapterProperty.bindBidirectional(workbookDataStore.activeChapterProperty)
        activeChunkProperty.bindBidirectional(workbookDataStore.activeChunkProperty)
        audioPluginViewModel.pluginNameProperty.bind(pluginNameBinding())
    }

    fun dock() {
        initializeListeners()
        loadTakes()
        openPlayers()
        highlightedChunkProperty.set(NO_HIGHLIGHT_INDEX)
    }

    fun undock() {
        removeListeners()
        closePlayers()
    }

    private fun initializeListeners() {
        workbookDataStore.activeWorkbookProperty.onChangeAndDoNowWithDisposer { workbook ->
            workbook?.let {
                getChapterList(workbook.target.chapters)
            }
        }.let(listeners::add)

        verseCountProperty.bind(
            Bindings.createIntegerBinding(
                {
                    // no verse count in chunk/verse page
                    if (activeChunkProperty.value != null) {
                        0
                    } else {
                        val projectAccessor = workbookDataStore.activeProjectFilesAccessorProperty
                        projectAccessor.value?.let {
                            it.getChapterText(
                                workbookDataStore.workbook.target.slug,
                                activeChapter.sort
                            ).size
                        } ?: 0
                    }
                },
                workbookDataStore.activeProjectFilesAccessorProperty,
                activeChunkProperty
            )
        )

        activeChapterProperty.onChangeAndDoNowWithDisposer { chapter ->
            setHasNextAndPreviousChapter()
            chapter?.let {
                getChunkList(chapter.chunks)
                recordable = it
            }
        }.let(listeners::add)

        activeChunkProperty.onChangeAndDoNowWithDisposer { chunk ->
            setHasNextAndPreviousChunk()
            if (chunk != null) {
                setTitle(chunk)
                // This will trigger loading takes
                recordable = chunk
            } else {
                activeChapterProperty.value?.let {
                    recordable = it
                }
            }
        }.let(listeners::add)

        recordableProperty.onChangeAndDoNowWithDisposer {
            clearDisposables()
            subscribeSelectedTakePropertyToRelay()
            loadTakes()
        }.let(listeners::add)

        workbookDataStore.sourceAudioProperty.onChangeAndDoNowWithDisposer {
            openSourceAudioPlayer()
        }.let(listeners::add)

        takeCardModels.onChangeAndDoNowWithDisposer {
            val animationMediator = ListAnimationMediator<ScriptureTakeCard>()
            takeCardViews.setAll(
                it.map { takeCardModel ->
                    ScriptureTakeCard().apply {
                        animationMediatorProperty.set(animationMediator)
                        takeProperty.set(takeCardModel.take)
                        audioPlayerProperty.set(takeCardModel.audioPlayer)
                        markerModelProperty.set(
                            VerseMarkerModel(
                                AudioFile(takeCardModel.take.file),
                                verseCountProperty.value
                            )
                        )
                        onChunkPlaybackUpdated = { chunkNumber -> highlightedChunkProperty.set(chunkNumber) }
                        selectedProperty.set(takeCardModel.selected)
                        takeLabelProperty.set(
                            MessageFormat.format(
                                FX.messages["takeTitle"],
                                FX.messages["take"],
                                takeCardModel.take.number
                            )
                        )
                        setOnTakeDelete {
                            error(
                                messages["deleteTakePrompt"],
                                messages["cannotBeUndone"],
                                ButtonType.YES,
                                ButtonType.NO,
                                title = messages["deleteTakePrompt"]
                            ) { button: ButtonType ->
                                if (button == ButtonType.YES) {
                                    deletedProperty.set(true)
                                    // trigger delete process after animation
                                    deletedProperty.onChangeOnce { isAnimating ->
                                        if (isAnimating == false) fireEvent(DeleteTakeEvent(takeCardModel.take))
                                    }
                                }
                            }
                        }
                        setOnTakeEdit {
                            fireEvent(
                                TakeEvent(takeCardModel.take, {}, TakeEvent.EDIT_TAKE)
                            )
                        }
                        setOnTakeSelected {
                            fireEvent(
                                TakeEvent(takeCardModel.take, {}, TakeEvent.SELECT_TAKE)
                            )
                        }
                    }
                }
            )
        }.let(listeners::add)
    }

    private fun removeListeners() {
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }

    fun nextChapter() {
        closePlayers()
        stepToChapter(StepDirection.FORWARD)
    }

    fun previousChapter() {
        closePlayers()
        stepToChapter(StepDirection.BACKWARD)
    }

    fun nextChunk() {
        closePlayers()
        stepToChunk(StepDirection.FORWARD)
    }

    fun previousChunk() {
        closePlayers()
        stepToChunk(StepDirection.BACKWARD)
    }

    private fun setHasNextAndPreviousChapter() {
        activeChapterProperty.value?.let { chapter ->
            if (chapterList.isNotEmpty()) {
                hasNextChapter.set(chapter.sort < chapterList.last().sort)
                hasPreviousChapter.set(chapter.sort > chapterList.first().sort)
            } else {
                hasNextChapter.set(false)
                hasPreviousChapter.set(false)
                chapterList.sizeProperty.onChangeOnce {
                    setHasNextAndPreviousChapter()
                }
            }
        } ?: run {
            hasNextChapter.set(false)
            hasPreviousChapter.set(false)
        }
    }

    private fun setHasNextAndPreviousChunk() {
        activeChunkProperty.value?.let { chunk ->
            if (chunkList.isNotEmpty()) {
                hasNextChunk.set(chunk.sort < chunkList.last().sort)
                hasPreviousChunk.set(chunk.sort > chunkList.first().sort)
            } else {
                hasNextChunk.set(false)
                hasPreviousChunk.set(false)
                chunkList.sizeProperty.onChangeOnce {
                    setHasNextAndPreviousChunk()
                }
            }
        } ?: run {
            hasNextChunk.set(false)
            hasPreviousChunk.set(false)
        }
    }

    private fun setTitle(chunk: Chunk) {
        title = MessageFormat.format(
            messages["chunkTitle"],
            messages[chunk.label],
            chunk.sort
        )
    }

    private fun getChapterList(chapters: Observable<Chapter>) {
        chapters
            .toList()
            .map { it.sortedBy { chapter -> chapter.sort } }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in getting the chapter list", e)
            }
            .subscribe { list ->
                chapterList.setAll(list)
            }
    }

    private fun getChunkList(chunks: Observable<Chunk>) {
        var draft = 0
        activeChunkSubscription?.dispose()
        chunkList.clear()
        activeChunkSubscription = chunks
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in getting the chunk list", e)
            }
            .subscribe {
                if (it.draftNumber > draft) {
                    draft = it.draftNumber
                    chunkList.removeIf { it.draftNumber < draft }
                }
                chunkList.add(it)
                chunkList.sortBy { it.sort }
            }
    }

    private fun stepToChapter(direction: StepDirection) {
        val amount = when (direction) {
            StepDirection.FORWARD -> 1
            StepDirection.BACKWARD -> -1
        }
        val nextIndex = chapterList.indexOf(activeChapter) + amount
        chapterList.elementAtOrNull(nextIndex)?.let { activeChapterProperty.set(it) }
        highlightedChunkProperty.set(NO_HIGHLIGHT_INDEX)
    }

    private fun stepToChunk(direction: StepDirection) {
        val amount = when (direction) {
            StepDirection.FORWARD -> 1
            StepDirection.BACKWARD -> -1
        }

        val currentChunks = chunkList.filter { it.draftNumber > 0 }
        currentChunks
            .indexOf(activeChunk)
            .let { currentIndex ->
                currentChunks.elementAtOrNull(currentIndex + amount)
                    ?.let { activeChunkProperty.set(it) }
            }
    }

    fun recordNewTake() {
        closePlayers()
        recordable?.let { rec ->
            contextProperty.set(PluginType.RECORDER)
            rec.audio.getNewTakeNumber()
                .flatMapMaybe { takeNumber ->
                    workbookDataStore.activeTakeNumberProperty.set(takeNumber)
                    audioPluginViewModel.getPlugin(PluginType.RECORDER)
                }
                .flatMapSingle { plugin ->
                    fire(PluginOpenedEvent(PluginType.RECORDER, plugin.isNativePlugin()))
                    audioPluginViewModel.record(rec)
                }
                .observeOnFx()
                .doOnError { e ->
                    logger.error("Error in recording a new take", e)
                }
                .onErrorReturn { TakeActions.Result.NO_PLUGIN }
                .subscribe { result: TakeActions.Result ->
                    fire(PluginClosedEvent(PluginType.RECORDER))
                    when (result) {
                        TakeActions.Result.NO_PLUGIN -> snackBarObservable.onNext(messages["noRecorder"])
                        TakeActions.Result.SUCCESS -> {
                            setMarker()
                            loadTakes()
                            workbookDataStore.updateSelectedTakesFile()
                        }
                        TakeActions.Result.NO_AUDIO -> {
                            setMarker()
                            loadTakes()
                        }
                    }
                }
        } ?: throw IllegalStateException("Recordable is null")
    }

    fun processTakeWithPlugin(takeEvent: TakeEvent, pluginType: PluginType) {
        closePlayers()
        contextProperty.set(pluginType)
        workbookDataStore.activeTakeNumberProperty.set(takeEvent.take.number)
        audioPluginViewModel
            .getPlugin(pluginType)
            .flatMapSingle { plugin ->
                fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
                when (pluginType) {
                    PluginType.EDITOR -> audioPluginViewModel.edit(recordable!!.audio, takeEvent.take)
                    PluginType.MARKER -> audioPluginViewModel.mark(recordable!!.audio, takeEvent.take)
                    else -> null
                }
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType", e)
            }
            .onErrorReturn { TakeActions.Result.NO_PLUGIN }
            .subscribe { result: TakeActions.Result ->
                fire(PluginClosedEvent(pluginType))
                when (result) {
                    TakeActions.Result.NO_PLUGIN -> snackBarObservable.onNext(messages["noEditor"])
                    TakeActions.Result.SUCCESS -> {
                        takeEvent.onComplete()
                        loadTakes()
                    }
                }
            }
    }

    fun selectTake(take: Take) {
        recordable?.audio?.selectTake(take) ?: throw IllegalStateException("Recordable is null")
        workbookDataStore.updateSelectedTakesFile()
        take.file.setLastModified(System.currentTimeMillis())
    }

    fun importTakes(files: List<File>) {
        showImportProgressDialogProperty.set(true)
        closePlayers()

        recordable?.let { rec ->
            files.toRxObservable()
                .subscribeOn(Schedulers.io())
                .flatMapCompletable { takeFile ->
                    audioPluginViewModel.import(rec, takeFile)
                }
                .observeOnFx()
                .doOnError { e ->
                    logger.error("Error in importing take", e)
                }
                .doFinally {
                    showImportProgressDialogProperty.set(false)
                }
                .subscribe(
                    {
                        showImportSuccessDialogProperty.set(true)
                        setMarker()
                        loadTakes()
                    },
                    {
                        showImportFailDialogProperty.set(true)
                    }
                )
        }
    }

    fun deleteTake(take: Take) {
        stopPlayers()
        val isTakeSelected = takeCardModels.any { it.take == take && it.selected }
        take.deletedTimestamp.accept(DateHolder.now())
        removeOnDeleted(take, isTakeSelected)
    }

    fun dialogTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                String.format(
                    messages["sourceDialogTitle"],
                    workbookDataStore.activeTakeNumberProperty.value,
                    audioPluginViewModel.pluginNameProperty.value
                )
            },
            audioPluginViewModel.pluginNameProperty,
            workbookDataStore.activeTakeNumberProperty
        )
    }

    fun dialogTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                String.format(
                    messages["sourceDialogMessage"],
                    workbookDataStore.activeTakeNumberProperty.value,
                    audioPluginViewModel.pluginNameProperty.value,
                    audioPluginViewModel.pluginNameProperty.value
                )
            },
            audioPluginViewModel.pluginNameProperty,
            workbookDataStore.activeTakeNumberProperty
        )
    }

    private fun pluginNameBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                when (contextProperty.get()) {
                    PluginType.RECORDER -> {
                        audioPluginViewModel.selectedRecorderProperty.get()?.name
                    }
                    PluginType.EDITOR -> {
                        audioPluginViewModel.selectedEditorProperty.get()?.name
                    }
                    PluginType.MARKER -> {
                        audioPluginViewModel.selectedMarkerProperty.get()?.name
                    }
                    null -> throw IllegalStateException("Action is not supported!")
                }
            },
            contextProperty,
            audioPluginViewModel.selectedRecorderProperty,
            audioPluginViewModel.selectedEditorProperty,
            audioPluginViewModel.selectedMarkerProperty
        )
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    private fun Take.isNotDeleted() = deletedTimestamp.value?.value == null

    private fun loadTakes() {
        recordable?.audio?.let { audio ->
            // selectedTakeProperty may not have been updated yet so ask for the current selected take
            val selected = audio.selected.value?.value

            val takes = audio.getAllTakes()
                .filter { it.isNotDeleted() }
                .map { take ->
                    take.mapToCardModel(take == selected)
                }
                .sortedWith(
                    compareByDescending<TakeCardModel> { it.selected }
                        .thenByDescending { it.take.file.lastModified() }
                )

            takeCardModels.setAll(takes)
        }
    }

    private fun setMarker() {
        if (activeChunkProperty.value == null) return

        recordable?.audio?.let { audio ->
            audio.selected.value?.value?.let {
                AudioFile(it.file).apply {
                    if (metadata.getCues().isEmpty()) {
                        metadata.addCue(0, activeChunk.start.toString())
                        update()
                    }
                }
            }
        }
    }

    private fun removeOnDeleted(take: Take, isTakeSelected: Boolean = false) {
        take.deletedTimestamp
            .filter { dateHolder -> dateHolder.value != null }
            .doOnError { e ->
                logger.error("Error in removing deleted take: $take", e)
            }
            .subscribe {
                removeFromTakes(take, isTakeSelected)
            }
            .let { disposables.add(it) }
    }

    private fun removeFromTakes(take: Take, autoSelect: Boolean = false) {
        Platform.runLater {
            takeCardModels.removeAll { it.take == take }
            if (autoSelect) {
                takeCardModels.firstOrNull()?.let {
                    selectTake(it.take)
                }
            }
        }
    }

    fun openPlayers() {
        takeCardModels.forEach { it.audioPlayer.load(it.take.file) }
        openSourceAudioPlayer()
    }

    fun openSourceAudioPlayer() {
        workbookDataStore.sourceAudioProperty.value?.let { source ->
            val audioPlayer = (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
            audioPlayer.loadSection(source.file, source.start, source.end)
            sourceAudioPlayerProperty.set(audioPlayer)
        } ?: sourceAudioPlayerProperty.set(null)
    }

    fun openTargetAudioPlayer() {
        workbookDataStore.targetAudioProperty.value?.let { target ->
            target.player.load(target.file)
        }
    }

    fun closePlayers() {
        takeCardModels.forEach { it.audioPlayer.close() }
        sourceAudioPlayerProperty.value?.close()
        sourceAudioPlayerProperty.set(null)
        workbookDataStore.targetAudioProperty.value?.player?.close()
        workbookDataStore.selectedChapterPlayerProperty.value?.close()
    }

    fun stopPlayers() {
        takeCardModels.forEach { it.audioPlayer.stop() }
        sourceAudioPlayerProperty.value?.stop()
        workbookDataStore.targetAudioProperty.value?.player?.stop()
    }

    private fun subscribeSelectedTakePropertyToRelay() {
        recordable?.audio?.let { audio ->
            audio
                .selected
                .doOnError { e ->
                    logger.error("Error in subscribing take to relay for audio: $audio", e)
                }
                .observeOnFx()
                .subscribe {
                    loadTakes()
                    workbookDataStore.updateSelectedChapterPlayer()
                }
                .let { disposables.add(it) }
        }
    }

    fun Take.mapToCardModel(selected: Boolean): TakeCardModel {
        val ap: IAudioPlayer = (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
        ap.load(this.file)
        return TakeCardModel(
            this,
            selected,
            ap,
            FX.messages["edit"].capitalizeString(),
            FX.messages["delete"].capitalizeString(),
            FX.messages["marker"].capitalizeString(),
            FX.messages["play"].capitalizeString(),
            FX.messages["pause"].capitalizeString()
        )
    }
}
