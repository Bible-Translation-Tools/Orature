/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import tornadofx.*
import java.text.MessageFormat
import java.util.concurrent.Callable
import javax.inject.Inject

class ChapterPageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ChapterPageViewModel::class.java)

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    val workbookDataStore: WorkbookDataStore by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    // List of content to display on the screen
    // Boolean tracks whether the content has takes associated with it
    private val allContent: ObservableList<CardData> = FXCollections.observableArrayList()
    val filteredContent: ObservableList<CardData> = FXCollections.observableArrayList()

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(ChapterPageViewModel::loading)

    val chapterPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val canCompileProperty = SimpleBooleanProperty()
    val isCompilingProperty = SimpleBooleanProperty()
    val selectedChapterTakeProperty = SimpleObjectProperty<Take>()
    val workChunkProperty = SimpleObjectProperty<CardData>()
    val noTakesProperty = SimpleBooleanProperty()

    val chapterCardProperty = SimpleObjectProperty<CardData>(CardData(workbookDataStore.chapter))
    val contextProperty = SimpleObjectProperty(PluginType.RECORDER)
    val currentTakeNumberProperty = SimpleObjectProperty<Int?>()

    val sourceAudioAvailableProperty = workbookDataStore.sourceAudioAvailableProperty
    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer?>(null)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val disposables = CompositeDisposable()
    private val navigator: NavigationMediator by inject()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        allContent
            .changes()
            .doOnError { e ->
                logger.error("Error in setting up content cards", e)
            }
            .subscribe {
                filteredContent.setAll(
                    allContent.filtered { cardData ->
                        cardData.item != ContentLabel.CHAPTER.value
                    }
                )
                checkCanCompile()
                setWorkChunk()
            }

        workbookDataStore.activeChapterProperty.onChangeAndDoNow { _chapter ->
            _chapter?.let { chapter ->
                loadChapterContents(chapter).subscribe()
                val chap = CardData(chapter)
                chapterCardProperty.set(chap)
            }
        }

        audioPluginViewModel.pluginNameProperty.bind(pluginNameBinding())

        chapterCardProperty.onChangeAndDoNow { chapter ->
            clearDisposables()
            chapter?.chapterSource?.let { chapterSource ->
                subscribeSelectedTakePropertyToRelay(chapterSource.audio)
            }
        }
    }

    fun breadcrumbTitleBinding(view: UIComponent): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                when {
                    workbookDataStore.activeChunkProperty.value != null ->
                        workbookDataStore.activeChunkProperty.value.let { chunk ->
                            MessageFormat.format(
                                messages["chunkTitle"],
                                messages[ContentLabel.of(chunk.contentType).value],
                                chunk.start
                            )
                        }
                        navigator.workspace.dockedComponentProperty.value == view -> messages["chunk"]
                    else -> messages["chapter"]
                }
            },
            navigator.workspace.dockedComponentProperty,
            workbookDataStore.activeChunkProperty
        )
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
        selectedChapterTakeProperty.value?.let {
            val player = (app as OtterApp).dependencyGraph.injectPlayer()
            player.load(it.file)
            chapterPlayerProperty.set(player)
        }
    }

    fun openSourceAudioPlayer() {
        workbookDataStore.sourceAudioProperty.value?.let { source ->
            val audioPlayer = (app as OtterApp).dependencyGraph.injectPlayer()
            audioPlayer.loadSection(source.file, source.start, source.end)
            sourceAudioPlayerProperty.set(audioPlayer)
        }
    }

    fun closePlayers() {
        chapterPlayerProperty.value?.close()
        chapterPlayerProperty.set(null)
        sourceAudioPlayerProperty.value?.close()
        sourceAudioPlayerProperty.set(null)
    }

    fun checkCanCompile() {
        val hasUnselected = filteredContent
            .filter { chunk ->
                chunk.chunkSource?.audio?.selected?.value?.value == null
            }
            .any()
        canCompileProperty.set(hasUnselected.not())
    }

    fun setWorkChunk() {
        if (filteredContent.isEmpty()) return

        val hasTakes = filteredContent.filter { chunk ->
            chunk.chunkSource?.audio?.getAllTakes()?.isNotEmpty() ?: false
        }.any()

        if (hasTakes) {
            val notSelected = filteredContent.filter { chunk ->
                chunk.chunkSource?.audio?.selected?.value?.value == null
            }.firstOrNull() ?: filteredContent.last()
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
        closePlayers()
        chapterCardProperty.value?.chapterSource?.let { rec ->
            contextProperty.set(PluginType.RECORDER)
            rec.audio.getNewTakeNumber()
                .flatMapMaybe { takeNumber ->
                    currentTakeNumberProperty.set(takeNumber)
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
                        TakeActions.Result.SUCCESS, TakeActions.Result.NO_AUDIO -> {}
                    }
                }
        } ?: throw IllegalStateException("Recordable is null")
    }

    fun processTakeWithPlugin(pluginType: PluginType) {
        selectedChapterTakeProperty.value?.let { take ->
            val audio = chapterCardProperty.value!!.chapterSource!!.audio
            closePlayers()
            contextProperty.set(pluginType)
            currentTakeNumberProperty.set(take.number)
            audioPluginViewModel
                .getPlugin(pluginType)
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
                    logger.error("Error in processing take with plugin type: $pluginType", e)
                }
                .onErrorReturn { TakeActions.Result.NO_PLUGIN }
                .subscribe { result: TakeActions.Result ->
                    currentTakeNumberProperty.set(null)
                    fire(PluginClosedEvent(pluginType))
                    when (result) {
                        TakeActions.Result.NO_PLUGIN -> snackBarObservable.onNext(messages["noEditor"])
                        else -> {
                            when (pluginType) {
                                PluginType.EDITOR, PluginType.MARKER -> {

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

            ConcatenateAudio(directoryProvider).execute(takes)
                .flatMapCompletable { file ->
                    audioPluginViewModel.import(chapter, file)
                }
                .subscribeOn(Schedulers.io())
                .doOnError { e ->
                    logger.error("Error in compiling chapter: $chapter", e)
                }
                .doFinally {
                    isCompilingProperty.set(false)
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
                    currentTakeNumberProperty.value,
                    audioPluginViewModel.pluginNameProperty.value
                )
            },
            audioPluginViewModel.pluginNameProperty,
            currentTakeNumberProperty
        )
    }

    fun dialogTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                String.format(
                    messages["sourceDialogMessage"],
                    currentTakeNumberProperty.value,
                    audioPluginViewModel.pluginNameProperty.value,
                    audioPluginViewModel.pluginNameProperty.value
                )
            },
            audioPluginViewModel.pluginNameProperty,
            currentTakeNumberProperty
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

    private fun loadChapterContents(chapter: Chapter): Completable {
        // Remove existing content so the user knows they are outdated
        allContent.clear()
        loading = true
        return chapter.chunks
            .map { CardData(it) }
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .toList()
            .doOnError { e ->
                logger.error("Error in loading chapter contents for chapter: $chapter", e)
            }
            .map { list: List<CardData> ->
                allContent.setAll(list)
            }.ignoreElement()
    }

    private fun clearDisposables() {
        disposables.clear()
    }

    private fun subscribeSelectedTakePropertyToRelay(audio: AssociatedAudio) {
        audio
            .selected
            .doOnError { e ->
                logger.error("Error in subscribing take to relay for audio: $audio", e)
            }
            .observeOnFx()
            .subscribe { takeHolder ->
                setSelectedChapterTake(takeHolder.value)
            }
            .let { disposables.add(it) }
    }
}
