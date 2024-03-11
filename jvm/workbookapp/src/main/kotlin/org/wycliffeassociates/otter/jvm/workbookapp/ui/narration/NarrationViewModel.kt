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

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import com.jakewharton.rxrelay2.ReplayRelay
import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.primitives.BOOK_TITLE_SORT
import org.wycliffeassociates.otter.common.data.primitives.CHAPTER_TITLE_SORT
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.*
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.*
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.event.*
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.NarratableItemData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.MARKER_AREA_WIDTH
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkerControl
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.NarrationWaveformRenderer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AppPreferencesStore
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.max

class NarrationViewModel : ViewModel() {
    private lateinit var rendererAudioReader: AudioFileReader
    private val logger = LoggerFactory.getLogger(NarrationViewModel::class.java)
    private val workbookDataStore: WorkbookDataStore by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val appPreferencesStore: AppPreferencesStore by inject()
    lateinit var audioPlayer: IAudioPlayer

    @Inject
    lateinit var narrationFactory: NarrationFactory

    @Inject
    lateinit var appPreferencesRepo: IAppPreferencesRepository

    private lateinit var narration: Narration
    private lateinit var renderer: NarrationWaveformRenderer
    private lateinit var narrationStateMachine: NarrationStateMachine
    val narrationStateProperty =
        SimpleObjectProperty<NarrationStateType>(NarrationStateType.IDLE_EMPTY) // TODO: maybe give this an init value

    private lateinit var volumeBar: VolumeBar
    val recordStartProperty = SimpleBooleanProperty()

    var recordStart by recordStartProperty
    val recordPauseProperty = SimpleBooleanProperty()
    var recordPause by recordPauseProperty
    val recordResumeProperty = SimpleBooleanProperty()
    var recordResume by recordResumeProperty
    val isRecordingProperty = SimpleBooleanProperty()
    var isRecording by isRecordingProperty
    val isRecordingAgainProperty = SimpleBooleanProperty()
    var isRecordingAgain by isRecordingAgainProperty
    val recordAgainVerseIndexProperty = SimpleObjectProperty<Int?>()
    var recordAgainVerseIndex by recordAgainVerseIndexProperty
    val isPlayingProperty = SimpleBooleanProperty(false)
    val recordingVerseIndex = SimpleIntegerProperty()

    val playingVerseProperty = SimpleObjectProperty<VerseMarker?>()
    var playingVerse by playingVerseProperty
    val playingVerseIndex = SimpleIntegerProperty(-1)
    val highlightedVerseIndex = SimpleIntegerProperty(-1)

    val hasUndoProperty = SimpleBooleanProperty()
    var hasUndo by hasUndoProperty
    val hasRedoProperty = SimpleBooleanProperty()
    var hasRedo by hasRedoProperty

    val chapterList: ObservableList<Chapter> = observableListOf()
    val chapterTitleProperty = SimpleStringProperty()
    val chapterTakeProperty = SimpleObjectProperty<Take>()
    val hasNextChapter = SimpleBooleanProperty()
    val hasPreviousChapter = SimpleBooleanProperty()
    val isModifyingTakeAudioProperty = SimpleBooleanProperty()
    val openLoadingModalProperty = SimpleBooleanProperty()
    private val navigator: NavigationMediator by inject()

    val chunkTotalProperty = SimpleIntegerProperty(0)
    val chunksList: ObservableList<Chunk> = observableListOf()
    val narratableList: ObservableList<NarratableItemData> = observableListOf()
    val recordedVerses = observableListOf<AudioMarker>()
    val hasVersesProperty = SimpleBooleanProperty()
    val lastRecordedVerseProperty = SimpleIntegerProperty()
    val audioPositionProperty = SimpleIntegerProperty()
    val totalAudioSizeProperty = SimpleIntegerProperty()
    private var onTaskRunnerIdle: () -> Unit = { }

    val hasAllItemsRecordedProperty = SimpleBooleanProperty()
    val potentiallyFinishedProperty = hasAllItemsRecordedProperty
        .and(isRecordingProperty.not())
        .and(isRecordingAgainProperty.not())
        .and(recordPauseProperty.not())
    val potentiallyFinished by potentiallyFinishedProperty

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)
    val pluginOpenedProperty = SimpleBooleanProperty(false)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val disposables = CompositeDisposable()
    private var disposers = mutableListOf<ListenerDisposer>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        hasVersesProperty.bind(recordedVerses.booleanBinding { it.isNotEmpty() })
        lastRecordedVerseProperty.bind(recordedVerses.sizeProperty)
        hasAllItemsRecordedProperty.bind(
            booleanBinding(recordedVerses, narratableList) {
                recordedVerses.isNotEmpty() && recordedVerses.size == narratableList.size
            }
        )

        subscribe<AppCloseRequestEvent> {
            logger.info("Received close event request")
            onUndock()
        }

        subscribe<NavigationRequestBlockedEvent> {
            if (isModifyingTakeAudioProperty.value) {
                openLoadingModalProperty.set(true)
                onTaskRunnerIdle = {
                    FX.eventbus.fire(it.navigationRequest)
                }
            }
        }

        narratableList.bind(chunksList) { chunk ->

            val marker = when (chunk.sort) {
                BOOK_TITLE_SORT -> recordedVerses.firstOrNull { it is BookMarker }
                CHAPTER_TITLE_SORT -> recordedVerses.firstOrNull { it is ChapterMarker }
                else -> recordedVerses.firstOrNull {
                    it.label == chunk.title && it is VerseMarker
                }
            }
            val hasRecording = when (chunk.sort) {
                BOOK_TITLE_SORT -> recordedVerses.any { it is BookMarker }
                CHAPTER_TITLE_SORT -> recordedVerses.any { it is ChapterMarker }
                else -> recordedVerses.any {
                    it.label == chunk.title && it is VerseMarker
                }
            }

            NarratableItemData(
                chunk,
                marker,
                hasRecording,
                chunk.sort - 1 <= recordedVerses.size
            )
        }

        recordedVerses.onChange {
            narratableList.forEachIndexed { idx, chunk ->
                // how much to pad the sort value due to injecting book and chapter titles
                // the first chapter will be the only chapter with a book title
                val sortPadding = if (workbookDataStore.chapter.sort == 1) 2 else 1

                chunk.previousChunksRecorded = chunk.chunk.sort + sortPadding - 1 <= recordedVerses.size
                chunk.marker = recordedVerses.getOrNull(idx)
            }
        }
    }

    fun onDock() {
        val workbook = workbookDataStore.workbook
        getChapterList(workbook.target.chapters)
            .observeOnFx()
            .subscribe(
                { chapter ->
                    loadChapter(chapter)
                }, { e ->
                    logger.error("Error loading chapter list", e)
                }
            )
            .let { disposables.add(it) }

        audioPositionProperty.onChangeWithDisposer { pos ->
            if (pos != null) updateHighlightedItem(pos.toInt())
        }.also { disposers.add(it) }

        appPreferencesRepo.sourceTextZoomRate()
            .subscribe { rate ->
                appPreferencesStore.sourceTextZoomRateProperty.set(rate)
            }.let { disposables.add(it) }
    }

    fun onUndock() {
        disposables.clear()
        disposers.forEach { it.dispose() }
        disposers.clear()
        closeNarrationAudio()
        narration.close()
        renderer.close()
    }

    private fun initializeNarration(chapter: Chapter) {
        narration = narrationFactory.create(workbookDataStore.workbook, chapter)
        audioPlayer = narration.getPlayer()
        audioPlayer.addEventListener { event: AudioPlayerEvent ->
            runLater {
                when (event) {
                    AudioPlayerEvent.PLAY -> {
                        isPlayingProperty.set(true)
                    }

                    AudioPlayerEvent.COMPLETE -> {
                        isPlayingProperty.set(false)

                        val playingVerse = playingVerseIndex.value

                        val newVerseList = narrationStateMachine.transition(
                            NarrationStateTransition.PAUSE_AUDIO_PLAYBACK,
                            playingVerse
                        )

                        val updated = narratableList.mapIndexed { idx, item ->
                            item.apply {
                                item.verseState = newVerseList[idx]
                            }
                        }
                        setVerseOptions(updated)
                        narratableList.setAll(updated)
                        refreshTeleprompter()
                    }

                    AudioPlayerEvent.PAUSE,

                    AudioPlayerEvent.STOP -> {
                        isPlayingProperty.set(false)
                    }

                    else -> {}
                }
            }
        }
        volumeBar = VolumeBar(narration.getRecorderAudioStream())
        subscribeActiveVersesChanged()
        subscribeTaskRunnerBusyChanged()
        updateRecordingState()
        rendererAudioReader = narration.audioReader
        rendererAudioReader.open()
        renderer = NarrationWaveformRenderer(
            AudioScene(
                rendererAudioReader,
                narration.getRecorderAudioStream(),
                isRecordingProperty.toObservable(),
                Screen.getMainScreen().width - 25 - 88,
                10,
                DEFAULT_SAMPLE_RATE
            )
        )
        totalAudioSizeProperty.set(rendererAudioReader.totalFrames)
        narrationStateMachine = NarrationStateMachine(narration.totalVerses)
        narrationStateMachine.initialize(narration.versesWithRecordings())
        subscribeNarrationStateChanged()
    }

    private fun updateRecordingState() {
        recordStart = recordedVerses.isEmpty()
        recordResume = recordedVerses.isNotEmpty()
    }

    private fun getChapterList(chapters: Observable<Chapter>): Single<Chapter> {
        return chapters
            .toList()
            .map { it.sortedBy { chapter -> chapter.sort } }
            .doOnError { e ->
                logger.error("Error in getting the chapter list", e)
            }
            .map { list ->
                val recentChapter = workbookDataStore.workbookRecentChapterMap
                    .getOrDefault(workbookDataStore.workbook, 1)

                val activeChapter = workbookDataStore.activeChapterProperty.value
                    ?: list.find { it.sort == recentChapter }
                    ?: list.first()

                workbookDataStore.activeChapterProperty.set(activeChapter)
                chapterList.setAll(list)
                activeChapter
            }
    }


    /**
     * Resets the properties and state of the ViewModel to prevent dirty state when moving to other chapters or
     * another narration project.
     */
    private fun resetState() {
        recordedVerses.clear()
        chunksList.clear()
        narratableList.clear()

        recordStartProperty.set(false)
        recordPauseProperty.set(false)
        recordResumeProperty.set(false)
        isRecordingProperty.set(false)
        isRecordingAgainProperty.set(false)
        recordAgainVerseIndexProperty.set(null)
        isPlayingProperty.set(false)
        recordingVerseIndex.set(-1)
        playingVerseProperty.set(null)
        playingVerseIndex.set(-1)
        highlightedVerseIndex.set(-1)
        hasUndoProperty.set(false)
        hasRedoProperty.set(false)
        audioPositionProperty.set(0)
        totalAudioSizeProperty.set(0)
    }

    private fun createPotentiallyFinishedChapterTake() {
        if (potentiallyFinished && (chapterTakeProperty.value == null || chapterTakeProperty.value.isDeleted())) {
            narration
                .createChapterTake()
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        chapterTakeProperty.set(it)
                    }, { e ->
                        logger.error(
                            "Error in creating a chapter take for ${chapterTitleProperty.value}",
                            e
                        )
                    }
                ).let { disposables.add(it) }
        } else if (chapterTakeProperty.value != null && hasAllItemsRecordedProperty.value == false) {
            // Deletes the chapter take because one currently exists, and we do not have all items recorded.
            narration.deleteChapterTake()
            chapterTakeProperty.set(null)
        }
    }


    fun processWithPlugin(pluginType: PluginType) {

        val getChapterTake = if (chapterTakeProperty.value != null) {
            Single.just(chapterTakeProperty.value)
        } else {
            narration.createChapterTakeWithAudio()
        }

        getChapterTake
            .doOnSubscribe {
                openLoadingModalProperty.set(true)
            }
            .doAfterSuccess { take ->
                openLoadingModalProperty.set(false)
                openTakeInPlugin(pluginType, take)
            }
            .subscribe()
    }

    private fun openTakeInPlugin(pluginType: PluginType, take: Take) {
        workbookDataStore.activeChapterProperty.value?.audio?.let { audio ->
            pluginContextProperty.set(pluginType)
            workbookDataStore.activeTakeNumberProperty.set(take.number)

            audioPluginViewModel
                .getPlugin(pluginType)
                .doOnError { e ->
                    logger.error("Error in processing take with plugin type: $pluginType, ${e.message}")
                }
                .flatMapSingle { plugin ->
                    pluginOpenedProperty.set(true)
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
                .onErrorReturn { PluginActions.Result.NO_PLUGIN }
                .subscribe { result: PluginActions.Result ->
                    logger.info("Returned from plugin with result: $result")
                    FX.eventbus.fire(PluginClosedEvent(pluginType))

                    when (result) {
                        PluginActions.Result.NO_PLUGIN -> FX.eventbus.fire(SnackBarEvent(messages["noEditor"]))
                        else -> {
                            when (pluginType) {
                                PluginType.EDITOR, PluginType.MARKER -> {
                                    FX.eventbus.fire(ChapterReturnFromPluginEvent())
                                }

                                else -> {
                                    logger.error("Plugin returned with result $result, plugintype: $pluginType did not match a known plugin.")
                                }
                            }
                        }
                    }
                }
        }
    }

    /**
     * If we are currently modifying the chapter take, the chapter navigation is postponed until the modification is
     * done, otherwise, the chapter navigation is performed immediately.
     *
     * @param chapterNumber the chapter to move to
     */
    fun deferNavigateChapterWhileModifyingTake(chapterNumber: Int) {
        if (isModifyingTakeAudioProperty.value) {
            openLoadingModalProperty.set(true)
            onTaskRunnerIdle = {
                navigateChapter(chapterNumber)
            }

        } else {
            navigateChapter(chapterNumber)
        }
    }

    /**
     * Called when changing the chapter from the chapter selector.
     *
     * This means that we are in a chapter, and moving to another chapter, so undock was not called in between.
     * Normally, undock would trim the audio, so we want to make sure that the audio is trimmed before moving to
     * the next chapter.
     *
     * @param chapterNumber the chapter to move to
     */
    fun navigateChapter(chapterNumber: Int) {
        if (::narration.isInitialized) {
            closeNarrationAudio()
            narration.close()
            renderer.close()
        }
        loadChapter(chapterNumber)
    }

    fun loadChapter(chapterNumber: Int) {
        chapterList
            .find { it.sort == chapterNumber }
            ?.let {
                loadChapter(it)
            }
    }

    fun loadChapter(chapter: Chapter) {

        logger.info("Loading chapter: ${chapter.sort}")
        resetState()

        chapterTitleProperty.set(
            MessageFormat.format(
                messages["chapterTitle"],
                messages["chapter"],
                chapter.title
            )
        )

        workbookDataStore.activeChapterProperty.set(chapter)
        workbookDataStore.updateLastSelectedChapter(chapter.sort)
        initializeNarration(chapter)

        chunksList.clear()
        loadChunks(chapter)

        setHasNextAndPreviousChapter(chapter)
        chapterTakeProperty.set(chapter.getSelectedTake())
    }

    private fun loadChunks(chapter: Chapter) {
        workbookDataStore
            .workbook
            .source
            .chapters
            .toList()
            .toObservable()
            .map {
                it.find { it.sort == chapter.sort }
            }
            .map { chapter ->
                chapter.chunks.take(1)
            }
            .flatMap { it }
            .map { injectChapterTitleText(chapter, it) }
            .observeOnFx()
            .subscribe(
                { chunks ->
                    chunksList.setAll(chunks)
                    chunkTotalProperty.set(chunks.size)
                },
                {},
                { resetTeleprompter() }
            )
    }

    /**
     * Injects chapter title text since the source may not have it.
     */
    private fun injectChapterTitleText(chapter: Chapter, chunks: List<Chunk>): List<Chunk> {
        val indexOfChapterTitle = chunks.indexOfFirst {
            it.sort == -1 && it.contentType == ContentType.TITLE && it.textItem.text == ""
        }
        if (indexOfChapterTitle >= 0) {
            val updatedChunks = chunks.toMutableList()
            val chapterTitle = chapterTitleProperty.value
            val updatedChapterTitle = Chunk(
                CHAPTER_TITLE_SORT,
                chapter.label,
                AssociatedAudio(ReplayRelay.create()),
                listOf(),
                TextItem(chapterTitle, MimeType.USFM),
                1,
                chunks.size,
                false,
                1,
                ContentType.TITLE
            )
            updatedChunks[indexOfChapterTitle] = updatedChapterTitle
            return updatedChunks
        } else {
            return chunks
        }
    }

    private fun clearTeleprompter() {
        narratableList.forEachIndexed { idx, chunk ->
            chunk.verseState = VerseItemState.RECORD_DISABLED
        }
        narratableList[0].verseState = VerseItemState.RECORD
        setVerseOptions(narratableList)
        narratableList.setAll(narratableList.toList())
        refreshTeleprompter()
        FX.eventbus.fire(TeleprompterSeekEvent(0))
    }

    private fun resetTeleprompter() {

        val statesWithoutRecording = listOf(
            VerseItemState.BEGIN_RECORDING,
            VerseItemState.RECORD,
            VerseItemState.RECORD_DISABLED
        )

        narratableList.forEachIndexed { idx, chunk ->

            if (chunk.verseState !in statesWithoutRecording) {
                chunk.verseState = VerseItemState.RECORD_AGAIN
            } else {
                chunk.verseState = VerseItemState.RECORD_DISABLED
            }
        }


        val lastIndex = narratableList.indexOfFirst { it.verseState in statesWithoutRecording }
        var scrollToVerse = 0

        if (lastIndex != -1) {
            narratableList.get(lastIndex).verseState = VerseItemState.RECORD
            scrollToVerse = lastIndex
        }

        setVerseOptions(narratableList)
        narratableList.setAll(narratableList.toList())

        refreshTeleprompter()
        FX.eventbus.fire(TeleprompterSeekEvent(scrollToVerse))
    }

    private fun setHasNextAndPreviousChapter(chapter: Chapter) {
        if (chapterList.isNotEmpty()) {
            hasNextChapter.set(chapter.sort < chapterList.last().sort)
            hasPreviousChapter.set(chapter.sort > chapterList.first().sort)
        } else {
            hasNextChapter.set(false)
            hasPreviousChapter.set(false)
            chapterList.sizeProperty.onChangeOnce {
                setHasNextAndPreviousChapter(chapter)
            }
        }
    }


    fun snackBarMessage(message: String) {
        snackBarObservable.onNext(message)
    }


    private fun setVerseOptions(verses: List<NarratableItemData>) {

        val isRecording = narrationStateProperty.value == NarrationStateType.RECORDING
        val isRecordingPaused = narrationStateProperty.value == NarrationStateType.RECORDING_PAUSED
        val isRecordingAgain = narrationStateProperty.value == NarrationStateType.RECORDING_AGAIN
        val isRecordAgainPaused = narrationStateProperty.value == NarrationStateType.RECORDING_AGAIN_PAUSED
        val isPlaying = narrationStateProperty.value == NarrationStateType.PLAYING

        verses.forEach {
            val isAnotherVerseRecordingPaused = isRecordingPaused && it.verseState != VerseItemState.RECORDING_PAUSED

            val hasRecording = it.verseState == VerseItemState.RECORD_AGAIN_PAUSED
                    || it.verseState == VerseItemState.RECORDING_PAUSED || it.verseState == VerseItemState.RECORD_AGAIN

            val isVerseRecordingPaused = it.verseState == VerseItemState.RECORDING_PAUSED


            it.playEnabled = hasRecording
                    && !isRecording
                    && !isRecordingAgain
                    && !isRecordAgainPaused
                    && !isPlaying
                    && !isAnotherVerseRecordingPaused

            it.editVerseEnabled = hasRecording
                    && !isRecording
                    && !isRecordingPaused
                    && !isRecordingAgain
                    && !isRecordAgainPaused
                    && !isPlaying

            it.recordAgainEnabled = hasRecording
                    && !isRecording
                    && !isRecordingAgain
                    && !isRecordAgainPaused
                    && !isPlaying
                    && !isVerseRecordingPaused
        }
    }

    fun play(verse: AudioMarker) {
        val verseIndex = recordedVerses.indexOf(verse)
        val newVerseList = narrationStateMachine.transition(NarrationStateTransition.PLAY_AUDIO, verseIndex)

        playingVerseIndex.set(verseIndex)
        renderer.clearActiveRecordingData()
        audioPlayer.pause()

        narration.loadSectionIntoPlayer(verse)

        // audioPlayer.seek(0)
        audioPlayer.play()

        val updated = narratableList.mapIndexed { idx, item -> item.apply { item.verseState = newVerseList[idx] } }
        setVerseOptions(updated)
        narratableList.setAll(updated)
        refreshTeleprompter()
    }

    fun playAll() {

        val newVerseList = narrationStateMachine.transition(NarrationStateTransition.PLAY_AUDIO, -1)
        logger.info("Playing all")
        playingVerseIndex.set(-1)
        renderer.clearActiveRecordingData()
        audioPlayer.pause()
        narration.loadChapterIntoPlayer()

        // audioPlayer.seek(0)
        audioPlayer.play()

        val updated = narratableList.mapIndexed { idx, item -> item.apply { item.verseState = newVerseList[idx] } }
        setVerseOptions(updated)
        narratableList.setAll(updated)
        refreshTeleprompter()
    }


    fun pausePlayback() {
        val newVerseList = narrationStateMachine.transition(NarrationStateTransition.PAUSE_AUDIO_PLAYBACK, -1)

        val updated = narratableList.mapIndexed { idx, item ->
            item.apply {
                item.verseState = newVerseList[idx]
            }
        }
        setVerseOptions(updated)
        narratableList.setAll(updated)
        refreshTeleprompter()

        logger.info("Pausing playback")
        audioPlayer.pause()
    }

    fun recordAgain(verseIndex: Int) {
        stopPlayer()

        narration.onRecordAgain(verseIndex)

        recordAgainVerseIndex = verseIndex
        recordingVerseIndex.set(verseIndex)
        isRecording = true
        isRecordingAgain = true
        recordPause = false

        refreshTeleprompter()
    }

    fun saveRecording(verseIndex: Int) {
        logger.info("Saving recording for: ${narration.totalVerses[verseIndex].formattedLabel}")

        stopPlayer()

        narration.onSaveRecording(verseIndex)

        recordAgainVerseIndex = null
        recordingVerseIndex.set(-1)
        isRecording = false
        isRecordingAgain = false
        recordPause = false

        renderer.clearActiveRecordingData()

        refreshTeleprompter()

        createPotentiallyFinishedChapterTake()
    }

    fun openInAudioPlugin(index: Int) {
        val file = narration.getSectionAsFile(index)
        processWithEditor(file, index)
    }

    fun onChapterReturnFromPlugin() {
        narration.loadFromSelectedChapterFile()
        recordedVerses.setAll(narration.activeVerses)
        updateRecordingState()

        refreshTeleprompter()

        // Indicates that we used a temporary take to edit the chapter
        if (hasAllItemsRecordedProperty.value == false) {
            // Deletes the wav file for the temporary take since it will not be referenced to again
            narration.deleteChapterTake(true)
        }
    }

    fun onNext(index: Int) {
        when {
            isRecording -> {
                narration.finalizeVerse(max(index - 1, 0))
                narration.onNewVerse(index)
                renderer.clearActiveRecordingData()
                recordingVerseIndex.set(index)
            }

            recordPause -> {
                recordPause = false
                recordResume = true
                recordingVerseIndex.set(-1)
            }

            else -> {}
        }

        refreshTeleprompter()
    }

    fun moveMarker(index: Int, delta: Int) {
        narration.onVerseMarkerMoved(index, delta)
    }

    /**
     * Clears the chapter to start over, resetting the teleprompter so that all verses are cleared and the first verse
     * is restored to the begin recording state.
     */
    fun restartChapter() {
        narration.onResetAll()

        narrationStateMachine.initialize(narration.versesWithRecordings())
        recordStart = true
        recordResume = false
        recordPause = false

        clearTeleprompter()
    }

    fun undo() {
        narration.undo()
        narrationStateMachine.initialize(narration.versesWithRecordings())
        recordPause = false

        resetTeleprompter()
    }

    fun redo() {
        narration.redo()
        narrationStateMachine.initialize(narration.versesWithRecordings())
        recordPause = false

        resetTeleprompter()
    }

    fun record(index: Int) {
        narration.onNewVerse(index)

        isRecording = true
        recordStart = false
        recordResume = false
        recordingVerseIndex.set(index)

        refreshTeleprompter()
    }

    fun pauseRecording(index: Int) {
        logger.info("Pausing recording for: ${narration.totalVerses[index].formattedLabel}")

        isRecording = false
        recordPause = true

        narration.pauseRecording()
        narration.finalizeVerse(index)
        renderer.clearActiveRecordingData()

        refreshTeleprompter()
    }


    fun pauseRecordAgain(index: Int) {
        logger.info("Pausing record again for: ${narration.totalVerses[index].formattedLabel}")

        isRecording = false
        recordPause = true

        narration.pauseRecording()
        narration.finalizeVerse(index)
        refreshTeleprompter()
    }

    fun resumeRecordingAgain() {
        logger.info("Resuming record again for: ${narration.totalVerses[recordingVerseIndex.value].formattedLabel}")

        stopPlayer()

        narration.resumeRecordingAgain()
        isRecording = true
        recordPause = false

        refreshTeleprompter()
    }

    fun resumeRecording() {
        logger.info("Resuming record ${narration.totalVerses[recordingVerseIndex.value].formattedLabel}")

        stopPlayer()

        narration.resumeRecording()

        isRecording = true
        recordPause = false

        refreshTeleprompter()
    }

    private fun stopPlayer() {
        audioPlayer.pause()
        playingVerse = null
    }

    private fun closeNarrationAudio() {
        narration.closeRecorder()
        narration.closeChapterRepresentation()
    }

    private fun processWithEditor(file: File, verseIndex: Int) {
        val pluginType = PluginType.EDITOR
        pluginContextProperty.set(pluginType)

        audioPluginViewModel.getPlugin(pluginType)
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType, ${e.message}")
            }
            .flatMapSingle { plugin ->
                pluginOpenedProperty.set(true)
                workbookDataStore.activeTakeNumberProperty.set(1)
                FX.eventbus.fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
                audioPluginViewModel.edit(file)
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType - $e")
            }
            .onErrorReturn { PluginActions.Result.NO_PLUGIN }
            .subscribe { result ->
                logger.info("Returned from plugin with result: $result")

                when (result) {
                    PluginActions.Result.NO_PLUGIN -> {
                        FX.eventbus.fire(SnackBarEvent(messages["noEditor"]))
                    }

                    else -> {
                        narration.onEditVerse(verseIndex, file)
                    }
                }
                FX.eventbus.fire(PluginClosedEvent(pluginType))
            }
    }

    private fun subscribeActiveVersesChanged() {
        recordedVerses.setAll(narration.activeVerses)
        hasUndo = narration.hasUndo()
        hasRedo = narration.hasRedo()

        narration
            .onActiveVersesUpdated
            .subscribe(
                { verses ->
                    totalAudioSizeProperty.set(rendererAudioReader.totalFrames)

                    val verseWasAdded = recordedVerses.size != verses.size

                    recordedVerses.setAll(verses)

                    hasUndo = narration.hasUndo()
                    hasRedo = narration.hasRedo()

                    if (verses.isNotEmpty()) {
                        val lastVerse = verses.getOrElse(lastRecordedVerseProperty.value) { verses.last() }.location

                        if (verseWasAdded) {
                            narration.seek(lastVerse)
                        }
                    } else {
                        narration.seek(0)
                    }

                    recordStart = recordedVerses.isEmpty()
                    recordResume = recordedVerses.isNotEmpty()
                    createPotentiallyFinishedChapterTake()
                },
                { e ->
                    logger.error("Error in active verses subscription", e)
                }
            )
            .let(disposables::add)
    }

    private fun subscribeTaskRunnerBusyChanged() {
        NarrationTakeModifier.status
            .doOnNext {
                val isIdle = (it == TaskRunnerStatus.IDLE)

                runLater {
                    isModifyingTakeAudioProperty.set(!isIdle)
                    navigator.blockNavigationEvents.set(!isIdle)

                    // TODO note: this gets hairy when working with PLAYING_WHILE_BOUNCING
                    if (isIdle && narrationStateMachine.getGlobalContext() == BouncingAudioState) {
                        narrationStateMachine.transition(NarrationStateTransition.SAVE_FINISHED, -1)
                    }

                    // Indicates that we have opened the saving model to interrupt either a chapter navigation or
                    // home navigation and that we should re-attempt the interrupted navigation
                    if (isIdle && openLoadingModalProperty.value) {
                        openLoadingModalProperty.set(false)
                        onTaskRunnerIdle()
                        onTaskRunnerIdle = { }
                    }
                }

            }
            .subscribe().let(disposables::add)
    }


    private fun subscribeNarrationStateChanged() {
        narrationStateMachine.currentState
            .doOnNext {
                it?.let {
                    narrationStateProperty.set(it.type)
                }
            }
            .subscribe().let(disposables::add)
    }

    fun drawWaveform(
        context: GraphicsContext,
        canvas: Canvas,
        markerNodes: ObservableList<VerseMarkerControl>
    ) {
        if (::renderer.isInitialized) {
            try {
                val position = narration.getLocationInFrames()
                runLater {
                    audioPositionProperty.set(position)
                }
                var reRecordLoc: Int? = null
                var nextVerseLoc: Int? = null
                if (isRecordingAgain) {
                    val reRecordingIndex = recordingVerseIndex.value
                    nextVerseLoc = recordedVerses.getOrNull(reRecordingIndex + 1)?.location
                    reRecordLoc = recordedVerses[reRecordingIndex].location
                }

                val viewports = renderer.draw(
                    context,
                    canvas,
                    position,
                    reRecordLoc,
                    nextVerseLoc
                )
                adjustMarkers(markerNodes, viewports, canvas.width.toInt())
            } catch (e: Exception) {
                logger.error("", e)
            }
        }
    }

    fun drawVolumebar(context: GraphicsContext, canvas: Canvas) {
        if (::renderer.isInitialized) {
            runLater {
                volumeBar.draw(context, canvas)
            }
        }
    }

    private fun adjustMarkers(
        markerNodes: ObservableList<VerseMarkerControl>,
        viewports: List<IntRange>,
        width: Int
    ) {
        val checkpointRAVI = recordAgainVerseIndex
        val adjustedWidth = if (checkpointRAVI == null) width else width / viewports.size
        for (i in markerNodes.indices) {
            val marker = markerNodes[i]
            if (marker.userIsDraggingProperty.value == true) continue

            val verse = marker.verseProperty.value
            var found = false
            for (viewPortIndex in viewports.indices) {
                val viewport = viewports[viewPortIndex]

                val checkpointRAVI = recordAgainVerseIndex
                if (checkpointRAVI != null) {
                    if (viewPortIndex != viewports.lastIndex && i > checkpointRAVI) continue
                    if (viewPortIndex == viewports.lastIndex && i <= checkpointRAVI) continue
                }

                if (verse.location in viewport) {
                    val viewportOffset = (width / viewports.size) * viewPortIndex
                    val newPos = framesToPixels(
                        verse.location - viewport.first,
                        adjustedWidth,
                        viewport.last - viewport.first
                    ).toDouble() - (MARKER_AREA_WIDTH / 2) + viewportOffset
                    runLater {
                        if (marker.layoutX != newPos) {
                            marker.layoutX = newPos
                        }
                    }
                    found = true
                }
            }
            runLater {
                marker.visibleProperty().set(found)
            }
        }
    }

    fun seekTo(frame: Int) {
        val wasPlaying = audioPlayer.isPlaying()
        audioPlayer.pause()
        narration.seek(frame, true)
        if (wasPlaying) audioPlayer.play()
    }

    fun seekPercent(percent: Double) {
        narration.seek(floor(narration.getDurationInFrames() * percent).toInt(), true)
    }

    fun seekToNext() {
        narration.seekToNext()
    }

    fun seekToPrevious() {
        narration.seekToPrevious()
    }

    private fun refreshTeleprompter() {
        FX.eventbus.fire(RefreshTeleprompter)
    }

    fun handleEvent(event: FXEvent) {
        val list = when (event) {
            is BeginRecordingEvent -> {
                record(event.index)
                narrationStateMachine.transition(NarrationStateTransition.RECORD, event.index)
            }

            is NextVerseEvent -> {
                onNext(event.index)
                narrationStateMachine.transition(NarrationStateTransition.NEXT, event.index)
            }

            is PauseRecordingEvent -> {
                pauseRecording(event.index)
                narrationStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, event.index)
            }


            is ResumeRecordingEvent -> {
                resumeRecording()
                narrationStateMachine.transition(NarrationStateTransition.RECORD, event.index)
            }

            is RecordVerseEvent -> {
                record(event.index)
                narrationStateMachine.transition(NarrationStateTransition.RECORD, event.index)
            }

            is RecordAgainEvent -> {
                recordAgain(event.index)
                narrationStateMachine.transition(NarrationStateTransition.RECORD_AGAIN, event.index)
            }

            is PauseRecordAgainEvent -> {
                pauseRecordAgain(event.index)
                narrationStateMachine.transition(NarrationStateTransition.PAUSE_RECORD_AGAIN, event.index)
            }

            is ResumeRecordingAgainEvent -> {
                resumeRecordingAgain()
                narrationStateMachine.transition(NarrationStateTransition.RESUME_RECORD_AGAIN, event.index)
            }

            is SaveRecordingEvent -> {
                saveRecording(event.index)
                narrationStateMachine.transition(NarrationStateTransition.SAVE, event.index)
            }

            else -> {
                return
            }
        }


        val updated = narratableList.mapIndexed { idx, item -> item.apply { item.verseState = list[idx] } }
        setVerseOptions(updated)
        narratableList.setAll(updated)
        refreshTeleprompter()
    }

    private fun updateHighlightedItem(audioPosition: Int) {
        when {
            isRecording -> highlightedVerseIndex.set(recordingVerseIndex.value)

            isRecordingAgain -> highlightedVerseIndex.set(recordAgainVerseIndex!!)

            else -> {
                val marker = narration.findMarkerAtPosition(audioPosition)
                val index = narratableList.indexOfFirst { it.marker == marker }
                highlightedVerseIndex.set(index)
            }
        }
    }
}