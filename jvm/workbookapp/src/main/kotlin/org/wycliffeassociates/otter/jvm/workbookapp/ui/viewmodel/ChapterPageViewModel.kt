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

import com.github.thomasnield.rxkotlinfx.changes
import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.RecordScripturePage
import tornadofx.*
import java.io.File
import java.util.concurrent.Callable
import javax.inject.Inject
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.domain.content.VerseByVerseChunking
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider


class ChapterPageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ChapterPageViewModel::class.java)

    val workbookDataStore: WorkbookDataStore by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var concatenateAudio: ConcatenateAudio

    @Inject
    lateinit var appPreferencesRepo: IAppPreferencesRepository

    // List of content to display on the screen
    // Boolean tracks whether the content has takes associated with it
    private val allContent: ObservableList<CardData> = FXCollections.observableArrayList()
    val filteredContent: ObservableList<CardData> = FXCollections.observableArrayList()

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(ChapterPageViewModel::loading)

    val canCompileProperty = SimpleBooleanProperty()
    val isCompilingProperty = SimpleBooleanProperty()
    val selectedChapterTakeProperty = SimpleObjectProperty<Take>()

    /**
     * WorkChunk is a first chunk to work with when you Begin Translation
     * or a next chunk with no takes when you Continue Translation
     */
    val workChunkProperty = SimpleObjectProperty<CardData>()
    val noTakesProperty = SimpleBooleanProperty()

    val chapterCardProperty = SimpleObjectProperty<CardData>()
    val contextProperty = SimpleObjectProperty(PluginType.RECORDER)

    val sourceAudioAvailableProperty = workbookDataStore.sourceAudioAvailableProperty
    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer?>(null)

    val showExportProgressDialogProperty = SimpleBooleanProperty(false)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val disposables = CompositeDisposable()
    private val navigator: NavigationMediator by inject()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        audioPluginViewModel.pluginNameProperty.bind(pluginNameBinding())

        filteredContent.onChange {
            checkCanCompile()
        }
    }

    fun dock() {
        chapterCardProperty.set(CardData(workbookDataStore.chapter))
        workbookDataStore.activeChapterProperty.value?.let { chapter ->
            updateLastSelectedChapter(chapter.sort)
            loadChapterContents(chapter).subscribe()
            val chap = CardData(chapter)
            chapterCardProperty.set(chap)
            subscribeSelectedTakePropertyToRelay(chapter.audio)
        }
        appPreferencesRepo.sourceTextZoomRate().subscribe { rate ->
            workbookDataStore.sourceTextZoomRateProperty.set(rate)
        }
        checkCanCompile()
    }

    fun undock() {
        selectedChapterTakeProperty.set(null)
        workbookDataStore.selectedChapterPlayerProperty.set(null)

        filteredContent.clear()
        allContent.clear()
        disposables.clear()
    }

    fun onCardSelection(cardData: CardData) {
        cardData.chapterSource?.let {
            workbookDataStore.activeChapterProperty.set(it)
        }
        // Chunk will be null if the chapter recording is opened. This needs to happen to update the recordable to
        // use the chapter recordable.
        workbookDataStore.activeChunkProperty.set(cardData.chunkSource)
    }

    fun openPlayers() {
        workbookDataStore.targetAudioProperty.value?.let { target ->
            target.player.load(target.file)
        }
        workbookDataStore.sourceAudioProperty.value?.let { source ->
            val audioPlayer = (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
            audioPlayer.loadSection(source.file, source.start, source.end)
            sourceAudioPlayerProperty.set(audioPlayer)
        }
    }

    fun closePlayers() {
        workbookDataStore.selectedChapterPlayerProperty.value?.close()
        workbookDataStore.targetAudioProperty.value?.player?.close()
        sourceAudioPlayerProperty.value?.close()
        sourceAudioPlayerProperty.set(null)
    }

    fun checkCanCompile() {
        val hasUnselected = filteredContent.any { chunk ->
            chunk.chunkSource?.audio?.selected?.value?.value == null
        }.or(filteredContent.isEmpty())

        canCompileProperty.set(hasUnselected.not())
    }

    fun setWorkChunk() {
        if (filteredContent.isEmpty()) { return }

        val hasTakes = filteredContent.any { chunk ->
            chunk.chunkSource?.audio?.getAllTakes()
                ?.any { it.deletedTimestamp.value?.value == null } ?: false
        }

        if (hasTakes) {
            val notSelected = filteredContent
                .firstOrNull { chunk ->
                    chunk.chunkSource?.audio?.selected?.value?.value == null
                } ?: filteredContent.last()
            noTakesProperty.set(false)
            workChunkProperty.set(notSelected)
        } else {
            noTakesProperty.set(true)
            workChunkProperty.set(filteredContent.first())
        }
    }

    fun setSelectedChapterTake(take: Take? = null) {
        selectedChapterTakeProperty.set(take)
        openPlayers()
    }

    fun recordChapter() {
        chapterCardProperty.value?.chapterSource?.let { rec ->
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
                        TakeActions.Result.SUCCESS, TakeActions.Result.NO_AUDIO -> {
                            /* no-op */
                        }
                    }
                }
        } ?: throw IllegalStateException("Recordable is null")
    }

    fun processTakeWithPlugin(pluginType: PluginType) {
        selectedChapterTakeProperty.value?.let { take ->
            val audio = chapterCardProperty.value!!.chapterSource!!.audio
            contextProperty.set(pluginType)
            workbookDataStore.activeTakeNumberProperty.set(take.number)
            audioPluginViewModel
                .getPlugin(pluginType)
                .doOnError { e ->
                    logger.error("Error in processing take with plugin type: $pluginType, ${e.message}")
                }
                .flatMapSingle { plugin ->
                    fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
                    when (pluginType) {
                        PluginType.EDITOR -> audioPluginViewModel.edit(audio, take)
                        PluginType.MARKER -> audioPluginViewModel.mark(audio, take)
                        else -> null
                    }
                }
                .observeOnFx()
                .doOnError { e ->
                    logger.error("Error in processing take with plugin type: $pluginType - $e")
                }
                .onErrorReturn { TakeActions.Result.NO_PLUGIN }
                .subscribe { result: TakeActions.Result ->
                    fire(PluginClosedEvent(pluginType))
                    when (result) {
                        TakeActions.Result.NO_PLUGIN -> snackBarObservable.onNext(messages["noEditor"])
                        else -> {
                            when (pluginType) {
                                PluginType.EDITOR, PluginType.MARKER -> {
                                    /* no-op */
                                }
                            }
                        }
                    }
                }
        }
    }

    fun compile() {
        canCompileProperty.value?.let {
            if (!it) return

            isCompilingProperty.set(true)

            val chapter = chapterCardProperty.value!!.chapterSource!!

            val takes = filteredContent.mapNotNull { chunk ->
                chunk.chunkSource?.audio?.selected?.value?.value?.file
            }

            var compiled: File? = null

            concatenateAudio.execute(takes)
                .flatMapCompletable { file ->
                    compiled = file
                    audioPluginViewModel.import(chapter, file)
                }
                .subscribeOn(Schedulers.io())
                .doOnError { e ->
                    logger.error("Error in compiling chapter: $chapter", e)
                }
                .doFinally {
                    isCompilingProperty.set(false)
                    compiled?.delete()
                }
                .observeOnFx()
                .subscribe()
        }
    }

    fun dialogTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
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
            Callable {
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

    fun pluginNameBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                when (contextProperty.value) {
                    PluginType.RECORDER -> {
                        audioPluginViewModel.selectedRecorderProperty.value?.name
                    }
                    PluginType.EDITOR -> {
                        audioPluginViewModel.selectedEditorProperty.value?.name
                    }
                    PluginType.MARKER -> {
                        audioPluginViewModel.selectedMarkerProperty.value?.name
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

    private fun updateLastSelectedChapter(chapterNumber: Int) {
        val workbookHash = workbookDataStore.workbook.hashCode()
        workbookDataStore.workbookRecentChapterMap[workbookHash] = chapterNumber - 1
    }

    private fun loadChapterContents(chapter: Chapter): Observable<CardData> {
        // Remove existing content so the user knows they are outdated
        allContent.clear()
        loading = true
        return chapter.chunks
            .map {
                CardData(it)
            }
            .map {
                buildTakes(it)
                it.player = getPlayer()
                it.onChunkOpen = ::onChunkOpen
                it.onTakeSelected = ::onTakeSelected
                it
            }
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in loading chapter contents for chapter: $chapter", e)
            }
            .map {
                if (it.chunkSource != null) {
                    if (it.chunkSource.draftNumber > 0) {
                        if (filteredContent.find { cont -> it.sort == cont.sort } == null) {
                            filteredContent.add(it)
                        }
                    }
                } else {
                    filteredContent.add(it)
                }

                filteredContent.removeIf { it.chunkSource != null && it.chunkSource.draftNumber < 0 }
                filteredContent.sortBy { it.sort }
                setWorkChunk()
                it
            }.observeOnFx()
    }

    private fun subscribeSelectedTakePropertyToRelay(audio: AssociatedAudio) {
        audio
            .selected
            .doOnError { e ->
                logger.error("Error in subscribing take to relay for audio: $audio", e)
            }
            .observeOnFx()
            .subscribe { takeHolder ->
                takeHolder.value?.let {
                    logger.info("Setting selected chapter take to ${takeHolder.value?.name}")
                    setSelectedChapterTake(takeHolder.value)
                    workbookDataStore.updateSelectedChapterPlayer()
                }
            }
            .let { disposables.add(it) }
    }

    private fun onChunkOpen(chunk: CardData) {
        onCardSelection(chunk)
        navigator.dock<RecordScripturePage>()
    }

    private fun onTakeSelected(chunk: CardData, take: TakeModel) {
        chunk.chunkSource?.audio?.selectTake(take.take)
        workbookDataStore.updateSelectedTakesFile()
        take.take.file.setLastModified(System.currentTimeMillis())
        buildTakes(chunk)
    }

    private fun buildTakes(chunkData: CardData) {
        chunkData.takes.clear()
        chunkData.chunkSource?.let { chunk ->
            val selected = chunk.audio.selected.value?.value
            chunk.audio.takes
                .filter { it.deletedTimestamp.value?.value == null }
                .filter { it.file.exists() }
                .map { take ->
                    setMarker(chunk.start.toString(), take)
                    take.mapToModel(take == selected)
                }.subscribe {
                    chunkData.takes.addAll(it)
                }.let {
                    disposables.add(it)
                }
        }
    }

    private fun setMarker(marker: String, take: Take) {
        AudioFile(take.file).apply {
            if (metadata.getCues().isEmpty()) {
                metadata.addCue(0, marker)
                update()
            }
        }
    }

    private fun Take.mapToModel(selected: Boolean): TakeModel {
        val audioPlayer = getPlayer()
        return TakeModel(this, selected, false, audioPlayer)
    }

    private fun getPlayer(): IAudioPlayer {
        return (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
    }

    fun chunkVerseByVerse() {
        val wkbk = workbookDataStore.activeWorkbookProperty.value
        val chapter = workbookDataStore.activeChapterProperty.value
        VerseByVerseChunking(directoryProvider, wkbk, chapter.addChunk, chapter.sort)
            .chunkVerseByVerse(wkbk.source.slug, 1)
    }

    fun resetChapter() {
        closePlayers()
        filteredContent.clear()
        val chapter = workbookDataStore.activeChapterProperty.value
        chapter.chunks.getValues(emptyArray()).forEach { chunk ->
            chunk.draftNumber = -1
            chunk.audio.getAllTakes()
                .filter { it.deletedTimestamp.value?.value == null }
                .forEach { take ->
                    take.deletedTimestamp.accept(DateHolder.now())
                }
        }
        chapter.reset()
    }
}
