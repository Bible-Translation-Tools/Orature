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
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.AudioScene
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.domain.narration.framesToPixels
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.TeleprompterItemState
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.TeleprompterStateMachine
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.TeleprompterStateTransition
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.event.*
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NarrationTextItemData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.MARKER_AREA_WIDTH
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.markers.VerseMarkerControl
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.NarrationWaveformRenderer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.max

private const val BOOK_TITLE_SORT = -2
private const val CHAPTER_TITLE_SORT = -1

class NarrationViewModel : ViewModel() {
    private lateinit var rendererAudioReader: AudioFileReader
    private val logger = LoggerFactory.getLogger(NarrationViewModel::class.java)
    private val workbookDataStore: WorkbookDataStore by inject()

    private val audioPluginViewModel: AudioPluginViewModel by inject()
    lateinit var audioPlayer: IAudioPlayer

    @Inject
    lateinit var narrationFactory: NarrationFactory

    private lateinit var narration: Narration
    private lateinit var renderer: NarrationWaveformRenderer
    private lateinit var teleprompterStateMachine: TeleprompterStateMachine

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
    val chapterTakeBusyProperty = SimpleBooleanProperty()

    val chunkTotalProperty = SimpleIntegerProperty(0)
    val chunksList: ObservableList<Chunk> = observableListOf()
    val narratableList: ObservableList<NarrationTextItemData> = observableListOf()
    val recordedVerses = observableListOf<AudioMarker>()
    val hasVersesProperty = SimpleBooleanProperty()
    val lastRecordedVerseProperty = SimpleIntegerProperty()
    val audioPositionProperty = SimpleIntegerProperty()
    val totalAudioSizeProperty = SimpleIntegerProperty()

    //FIXME: Refactor this if and when Chunk entries are officially added for Titles in the Workbook
    val numberOfTitlesProperty = SimpleIntegerProperty(0)
    val hasAllVersesRecordedProperty = chunkTotalProperty
        .eq(recordedVerses.sizeProperty.minus(numberOfTitlesProperty))
    val potentiallyFinishedProperty = hasAllVersesRecordedProperty
        .and(isRecordingProperty.not())
        .and(isRecordingAgainProperty.not())
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

        subscribe<AppCloseRequestEvent> {
            logger.info("Received close event request")
            onUndock()
        }

        narratableList.bind(chunksList) { chunk ->

            //FIXME: Refactor this if and when Chunk entries are officially added for Titles in the Workbook
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

            NarrationTextItemData(
                chunk,
                marker,
                hasRecording,
                chunk.sort - 1 <= recordedVerses.size
            )
        }

        recordedVerses.onChange {
            narratableList.forEachIndexed { idx, chunk ->

                //FIXME: Refactor this if and when Chunk entries are officially added for Titles in the Workbook
                val hasRecording = when (chunk.chunk.sort) {
                    BOOK_TITLE_SORT -> recordedVerses.any { it is BookMarker }
                    CHAPTER_TITLE_SORT -> recordedVerses.any { it is ChapterMarker }
                    else -> recordedVerses.any {
                        val matchingChunk = chunk.chunk.title == it.label && it is VerseMarker
                        matchingChunk
                    }
                }
                // how much to pad the sort value due to injecting book and chapter titles
                // the first chapter will be the only chapter with a book title
                val sortPadding = if (workbookDataStore.chapter.sort == 1) 2 else 1

                chunk.hasRecording = hasRecording
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
    }

    fun onUndock() {
        disposables.clear()
        disposers.forEach { it.dispose() }
        disposables.clear()
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
                    AudioPlayerEvent.PLAY -> isPlayingProperty.set(true)

                    AudioPlayerEvent.COMPLETE,
                    AudioPlayerEvent.PAUSE,
                    AudioPlayerEvent.STOP -> isPlayingProperty.set(false)

                    else -> {}
                }
            }
        }
        volumeBar = VolumeBar(narration.getRecorderAudioStream())
        subscribeActiveVersesChanged()
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
        teleprompterStateMachine = TeleprompterStateMachine(narration.totalVerses)
        teleprompterStateMachine.initialize(narration.versesWithRecordings())
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
                val chapterToResume = list.firstOrNull { !it.hasSelectedAudio() } ?: list.first()
                val activeChapter = workbookDataStore.activeChapterProperty.value ?: chapterToResume
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
        if (potentiallyFinished) {
            chapterTakeBusyProperty.set(true)
            logger.info("Chapter is potentially finished, creating a chapter take")
            narration
                .createChapterTake()
                .subscribeOn(Schedulers.io())
                .doFinally {
                    chapterTakeBusyProperty.set(false)
                }
                .subscribe(
                    {
                        chapterTakeProperty.set(it)
                        logger.info("Created a chapter take for ${chapterTitleProperty.value}")
                    }, { e ->
                        logger.error(
                            "Error in creating a chapter take for ${chapterTitleProperty.value}",
                            e
                        )
                    }
                ).let { disposables.add(it) }
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

        chapter
            .chunkCount
            .toObservable()
            .observeOnFx()
            .subscribe {
                chunkTotalProperty.set(it)
            }

        workbookDataStore.activeChapterProperty.set(chapter)
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
            .map { insertTitles(chapter, it) }
            .observeOnFx()
            .subscribe(
                { chunksList.setAll(it) },
                {},
                { resetTeleprompter() }
            )
    }

    /**
     * //FIXME remove this if and when titles are added to the database/workbook
     *
     * Inserts a Chunk for the Book and Chapter titles since the database and workbook do not have this data
     */
    private fun insertTitles(chapter: Chapter, chunks: List<Chunk>): List<Chunk> {
        val chunksWithTitles = chunks.toMutableList()
        val chapterTitle = chapterTitleProperty.value
        var numberOfTitles = 1
        chunksWithTitles.add(
            0,
            Chunk(
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
        )
        val addBookTitle = chapter.sort == 1
        if (addBookTitle) {
            val book = workbookDataStore.workbook.source
            chunksWithTitles.add(
                0,
                Chunk(
                    BOOK_TITLE_SORT,
                    book.label,
                    AssociatedAudio(ReplayRelay.create()),
                    listOf(),
                    TextItem(book.title, MimeType.USFM),
                    1,
                    chunks.size,
                    false,
                    1,
                    ContentType.TITLE
                )
            )
            numberOfTitles = 2
        }
        numberOfTitlesProperty.set(numberOfTitles)
        return chunksWithTitles
    }

    private fun clearTeleprompter() {
        narratableList.forEachIndexed { idx, chunk ->
            chunk.state = TeleprompterItemState.RECORD_DISABLED
        }
        narratableList[0].state = TeleprompterItemState.RECORD
        refreshTeleprompter()
        FX.eventbus.fire(TeleprompterSeekEvent(0))
    }

    private fun resetTeleprompter() {
        narratableList.forEachIndexed { idx, chunk ->
            if (chunk.hasRecording) {
                chunk.state = TeleprompterItemState.RECORD_AGAIN
            } else {
                chunk.state = TeleprompterItemState.RECORD_DISABLED
            }
        }
        val lastIndex = narratableList.indexOfFirst { !it.hasRecording }
        var scrollToVerse = 0

        if (lastIndex != -1) {
            narratableList.get(lastIndex).state = TeleprompterItemState.RECORD
            scrollToVerse = lastIndex
        }

        narratableList.setAll(narratableList.map { it })

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

    fun play(verse: AudioMarker) {
        playingVerseIndex.set(recordedVerses.indexOf(verse))
        renderer.clearActiveRecordingData()
        audioPlayer.pause()

        narration.loadSectionIntoPlayer(verse)

        // audioPlayer.seek(0)
        audioPlayer.play()
    }

    fun playAll() {
        logger.info("Playing all")
        playingVerseIndex.set(-1)
        renderer.clearActiveRecordingData()
        audioPlayer.pause()
        narration.loadChapterIntoPlayer()

        // audioPlayer.seek(0)
        audioPlayer.play()
    }

    fun pausePlayback() {
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
        recordingVerseIndex.set(verseIndex)
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
        teleprompterStateMachine.initialize(narration.versesWithRecordings())
        recordStart = true
        recordResume = false
        recordPause = false

        clearTeleprompter()
    }

    fun undo() {
        narration.undo()
        teleprompterStateMachine.initialize(narration.versesWithRecordings())
        recordPause = false

        resetTeleprompter()
    }

    fun redo() {
        narration.redo()
        teleprompterStateMachine.initialize(narration.versesWithRecordings())
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
            volumeBar.draw(context, canvas)
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
                teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, event.index)
            }

            is NextVerseEvent -> {
                onNext(event.index)
                teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, event.index)
            }

            is PauseRecordingEvent -> {
                pauseRecording(event.index)
                teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, event.index)
            }


            is ResumeRecordingEvent -> {
                resumeRecording()
                teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, event.index)
            }

            is RecordVerseEvent -> {
                record(event.index)
                teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, event.index)
            }

            is RecordAgainEvent -> {
                recordAgain(event.index)
                teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD_AGAIN, event.index)
            }

            is PauseRecordAgainEvent -> {
                pauseRecordAgain(event.index)
                teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORD_AGAIN, event.index)
            }

            is ResumeRecordingAgainEvent -> {
                resumeRecordingAgain()
                teleprompterStateMachine.transition(TeleprompterStateTransition.RESUME_RECORD_AGAIN, event.index)
            }

            is SaveRecordingEvent -> {
                saveRecording(event.index)
                teleprompterStateMachine.transition(TeleprompterStateTransition.SAVE, event.index)
            }

            else -> {
                return
            }
        }
        val updated = narratableList.mapIndexed { idx, item -> item.apply { item.state = list[idx] } }
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