package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.toObservable
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
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
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

    val hasUndoProperty = SimpleBooleanProperty()
    var hasUndo by hasUndoProperty
    val hasRedoProperty = SimpleBooleanProperty()
    var hasRedo by hasRedoProperty

    val chapterList: ObservableList<Chapter> = observableListOf()
    val chapterTitleProperty = SimpleStringProperty()
    val chapterTakeProperty = SimpleObjectProperty<Take>()
    val hasNextChapter = SimpleBooleanProperty()
    val hasPreviousChapter = SimpleBooleanProperty()

    val chunkTotalProperty = SimpleIntegerProperty(0)
    val chunksList: ObservableList<Chunk> = observableListOf()
    val narratableList: ObservableList<NarrationTextItemData> = observableListOf()

    val recordedVerses = observableListOf<VerseMarker>()
    val hasVersesProperty = SimpleBooleanProperty()
    val lastRecordedVerseProperty = SimpleIntegerProperty()
    val audioPositionProperty = SimpleIntegerProperty()
    val totalAudioSizeProperty = SimpleIntegerProperty()

    val potentiallyFinishedProperty = chunkTotalProperty.eq(recordedVerses.sizeProperty)
    val potentiallyFinished by potentiallyFinishedProperty

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val disposables = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        hasVersesProperty.bind(recordedVerses.booleanBinding { it.isNotEmpty() })
        lastRecordedVerseProperty.bind(recordedVerses.sizeProperty)

        subscribe<AppCloseRequestEvent> {
            logger.info("Received close event request")
            onUndock()
        }

        narratableList.bind(chunksList) { chunk ->
            NarrationTextItemData(
                chunk,
                recordedVerses.firstOrNull { it.label == chunk.title },
                recordedVerses.any { it.label == chunk.title },
                chunk.sort - 1 <= recordedVerses.size
            )
        }

        recordedVerses.onChange {
            narratableList.forEachIndexed { idx, chunk ->
                chunk.hasRecording = recordedVerses.any {
                    val matchingChunk = chunk.chunk.title == it.label
                    matchingChunk
                }
                chunk.previousChunksRecorded = chunk.chunk.sort - 1 <= recordedVerses.size
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
    }

    fun onUndock() {
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
                    AudioPlayerEvent.COMPLETE, AudioPlayerEvent.PAUSE, AudioPlayerEvent.STOP -> isPlayingProperty.set(
                        false
                    )

                    else -> {}
                }
            }
        }
        volumeBar = VolumeBar(narration.getRecorderAudioStream())
        subscribeActiveVersesChanged()
        updateRecordingState()
        rendererAudioReader = narration.audioReader
        renderer = NarrationWaveformRenderer(
            // NarrationAudioScene(
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
                runLater {
                    workbookDataStore.activeChapterProperty.set(chapterToResume)
                }
                chapterList.setAll(list)
                chapterToResume
            }
    }

    private fun resetState() {
        if (::narration.isInitialized && narration != null) {
            closeNarrationAudio()
            narration.close()
            renderer.close()
        }

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
        hasUndoProperty.set(false)
        hasRedoProperty.set(false)
        audioPositionProperty.set(0)
        totalAudioSizeProperty.set(0)
    }

    fun loadChapter(chapterNumber: Int) {
        chapterList
            .elementAtOrNull(chapterNumber)
            ?.let {
                loadChapter(it)
            }
    }

    fun loadChapter(chapter: Chapter) {
        resetState()

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
        chapterTitleProperty.set(
            MessageFormat.format(
                messages["chapterTitle"],
                messages["chapter"],
                chapter.title
            )
        )
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
            .flatMap { Observable.fromIterable(it) }
            .observeOnFx()
            .subscribe({ chunksList.add(it) }, {}, {
                resetTeleprompter()
            })
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
        // TODO: note, this is hit when navigating to a new chapter. The states are correct, however, they are not
        //  reflected in the teleprompterListView.
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

    fun play(verse: VerseMarker) {
        playingVerseIndex.set(recordedVerses.indexOf(verse))
        renderer.clearActiveRecordingData()
        audioPlayer.pause()

        narration.loadSectionIntoPlayer(verse)

        // audioPlayer.seek(0)
        audioPlayer.play()
    }

    fun playAll() {
        playingVerseIndex.set(-1)
        renderer.clearActiveRecordingData()
        audioPlayer.pause()
        narration.loadChapterIntoPlayer()
        // audioPlayer.seek(0)
        audioPlayer.play()
    }

    fun pausePlayback() {
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
        stopPlayer()

        narration.onSaveRecording(verseIndex)

        recordAgainVerseIndex = null
        recordingVerseIndex.set(verseIndex)
        isRecording = false
        isRecordingAgain = false
        recordPause = false

        renderer.clearActiveRecordingData()

        refreshTeleprompter()
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
        logger.info("Moving marker ${index} by $delta frames")
        narration.onVerseMarkerMoved(index, delta)
    }

    fun toggleRecording(index: Int) {
        when {
            isRecording && !isRecordingAgain -> pauseRecording(index)
            isRecording && isRecordingAgain -> stopRecordAgain()
            recordPause -> resumeRecording()
            recordStart || recordResume -> record(index)
            else -> {
                logger.error("Toggle recording is in the else state.")
            }
        }

        refreshTeleprompter()
    }

    fun resetChapter() {
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
        isRecording = false
        recordPause = true

        narration.pauseRecording()
        narration.finalizeVerse(index)
        renderer.clearActiveRecordingData()

        refreshTeleprompter()
    }


    fun pauseRecordAgain(index: Int) {
        isRecording = false
        recordPause = true

        narration.pauseRecording()
        narration.finalizeVerse(index)
        refreshTeleprompter()
    }

    fun resumeRecordingAgain() {
        stopPlayer()

        narration.resumeRecordingAgain()
        isRecording = true
        recordPause = false

        refreshTeleprompter()
    }

    fun resumeRecording() {
        stopPlayer()

        narration.resumeRecording()

        isRecording = true
        recordPause = false

        refreshTeleprompter()
    }

    fun stopRecordAgain() {
        narration.pauseRecording()
        recordAgainVerseIndex = null
        isRecording = false
        isRecordingAgain = false

        recordPause = false
        recordResume = true

        renderer.clearActiveRecordingData()

        recordAgainVerseIndex?.let { verseIndex ->
            narration.finalizeVerse(verseIndex)
        }

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
                        val lastVerse = verses.getOrElse(lastRecordedVerseProperty.value, { verses.last() }).location

                        if (verseWasAdded) {
                            narration.seek(lastVerse)
                        }
                    } else {
                        narration.seek(0)
                    }

                    recordStart = recordedVerses.isEmpty()
                    recordResume = recordedVerses.isNotEmpty()

                    if (potentiallyFinished) {
                        logger.info("Chapter is potentially finished, creating a chapter take")
                        narration
                            .createChapterTake()
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                {
                                    logger.info("Created a chapter take for ${chapterTitleProperty.value}")
                                }, { e ->
                                    logger.error(
                                        "Error in creating a chapter take for ${chapterTitleProperty.value}",
                                        e
                                    )
                                }
                            )
                    }
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
                    val nextChunk = chunksList.getOrNull(reRecordingIndex + 1)
                    if (nextChunk != null) {
                        val next = recordedVerses.firstOrNull { it.label == nextChunk.title }
                        if (next != null) {
                            nextVerseLoc = next.location
                        }
                    }

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
                        marker.visibleProperty().set(true)
                        if (marker.layoutX != newPos) {
                            marker.layoutX = newPos
                        }
                    }
                    found = true
                }
            }
            if (!found) {
                runLater {
                    marker.visibleProperty().set(false)
                }
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
}