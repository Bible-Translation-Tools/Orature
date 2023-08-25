package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.ExistingAndIncomingAudioRenderer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import javax.inject.Inject
import kotlin.math.max

class NarrationViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(NarrationViewModel::class.java)

    private val workbookDataStore: WorkbookDataStore by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    var audioPlayer = AudioPlayerController()

    @Inject
    lateinit var narrationFactory: NarrationFactory
    private lateinit var narration: Narration

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

    val playingVerseProperty = SimpleObjectProperty<VerseMarker?>()
    var playingVerse by playingVerseProperty

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

    val recordedVerses = observableListOf<VerseMarker>()
    val hasVersesProperty = SimpleBooleanProperty()
    val lastRecordedVerseProperty = SimpleIntegerProperty()

    val potentiallyFinishedProperty = chunkTotalProperty.eq(recordedVerses.sizeProperty)
    val potentiallyFinished by potentiallyFinishedProperty

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val listeners = mutableListOf<ListenerDisposer>()
    private val disposables = CompositeDisposable()


    var mockRecordedVerseMarkers = observableListOf<VerseMarker>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        hasVersesProperty.bind(recordedVerses.booleanBinding { it.isNotEmpty() })
        lastRecordedVerseProperty.bind(recordedVerses.integerBinding { it.size })

        for(i in 0 until 5) {
            mockRecordedVerseMarkers.add(VerseMarker(0 + 44100 * 2 * (i) + 1, 44100 * 2 * (i + 1), 44100 * 2 * (i + 1)))
        }
    }

    fun onDock() {
        val workbook = workbookDataStore.workbook
        getChapterList(workbook.target.chapters)
            .observeOnFx()
            .subscribe { chapter ->
                loadChapter(chapter)
            }
    }

    fun onUndock() {
        listeners.forEach(ListenerDisposer::dispose)
        disposables.dispose()

        closeNarrationAudio()
    }

    private fun initializeNarration(chapter: Chapter) {
        narration = narrationFactory.create(workbookDataStore.workbook, chapter)
        subscribeActiveVersesChanged()
        updateRecordingState()
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
                val chapterToResume = list.first { !it.hasSelectedAudio() }
                runLater {
                    workbookDataStore.activeChapterProperty.set(chapterToResume)
                }
                chapterList.setAll(list)
                chapterToResume
            }
    }

    fun getRecorderAudioStream(): Observable<ByteArray> {
        return narration.getRecorderAudioStream()
    }

    fun getExistingAudioFileReader(): AudioFileReader {
        return narration.audioReader
    }

    var existingAndIncomingAudioRendererIsInitialized = SimpleBooleanProperty(false)
    var existingAndIncomingAudioRenderer : ExistingAndIncomingAudioRenderer? = null
    val recordingStatus: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    fun initializeExistingAndIncomingAudioRenderer() {
        // ============= TODO: figure out where to actually initialize this stuff =============
        // Initializes properties / variables used in AudioWorkspace
        isRecordingProperty.addListener{_, old, new ->
            recordingStatus.onNext(new)
        }

        existingAndIncomingAudioRenderer = ExistingAndIncomingAudioRenderer(
            getExistingAudioFileReader(),
            getRecorderAudioStream(),
            recordingStatus,
            1920,
            10
        )

        existingAndIncomingAudioRendererIsInitialized.set(true)

        // ====================================================================================
    }

    fun loadChapter(chapter: Chapter) {
        chapter
            .chunkCount
            .toObservable()
            .observeOnFx()
            .subscribe {
                chunkTotalProperty.set(it)
            }

        workbookDataStore.activeChapterProperty.set(chapter)
        initializeNarration(chapter)

        initializeExistingAndIncomingAudioRenderer()

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
                chapter.getDraft()
            }.flatMap { it }
            .observeOnFx()
            .subscribe { chunksList.add(it) }
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
        if (playingVerse?.label == verse.label) {
            audioPlayer.toggle()
        } else {
            audioPlayer.pause()

            narration.loadSectionIntoPlayer(verse)

            audioPlayer.load(narration.getPlayer())
            audioPlayer.seek(0)
            audioPlayer.play()

            playingVerse = verse
        }
    }

    fun recordAgain(verseIndex: Int) {
        stopPlayer()

        narration.onRecordAgain(verseIndex)

        recordAgainVerseIndex = verseIndex
        isRecording = true
        isRecordingAgain = true
        recordPause = false
    }

    fun openInAudioPlugin(index: Int) {
        val file = narration.getSectionAsFile(index)
        processWithEditor(file, index)
    }

    fun onChapterReturnFromPlugin() {
        narration.loadFromSelectedChapterFile()
        recordedVerses.setAll(narration.activeVerses)
        updateRecordingState()
    }

    fun onNext(index: Int) {
        when {
            isRecording -> {
                narration.finalizeVerse(max(index - 1, 0))
                narration.onNewVerse(index)
            }

            recordPause -> {
                recordPause = false
                recordResume = true
            }

            else -> {}
        }
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
    }

    fun resetChapter() {
        narration.onResetAll()

        recordStart = true
        recordResume = false
        recordPause = false
    }

    fun undo() {
        narration.undo()

        recordPause = false
    }

    fun redo() {
        narration.redo()

        recordPause = false
    }

    private fun record(index: Int) {
        stopPlayer()

        narration.onNewVerse(index)

        isRecording = true
        recordStart = false
        recordResume = false
    }

    private fun pauseRecording(index: Int) {
        isRecording = false
        recordPause = true

        narration.pauseRecording()
        narration.finalizeVerse(index)
    }

    private fun resumeRecording() {
        stopPlayer()

        narration.resumeRecording()

        isRecording = true
        recordPause = false
    }

    private fun stopRecordAgain() {
        recordAgainVerseIndex?.let { verseIndex ->
            narration.pauseRecording()
            narration.finalizeVerse(verseIndex)

            recordAgainVerseIndex = null
            isRecording = false
            isRecordingAgain = false

            recordPause = false
            recordResume = true
        }
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

        narration.onActiveVersesUpdated.subscribe {
            recordedVerses.setAll(it)

            hasUndo = narration.hasUndo()
            hasRedo = narration.hasRedo()

            recordStart = recordedVerses.isEmpty()
            recordResume = recordedVerses.isNotEmpty()

            // Whenever the active audio changes, we need to update the waveform data
            // TODO: probably place this in a function or to a listener to RecordedVersesChanged
            existingAndIncomingAudioRenderer?.clearData()
            existingAndIncomingAudioRenderer?.fillExistingAudioHolder()
            existingAndIncomingAudioRenderer?.bytesFromIncomingProperty?.set(0)

        }.let(disposables::add)
    }
}