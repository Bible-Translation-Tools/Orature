package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
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
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.NarrationWaveformRenderer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import javax.inject.Inject
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
    val audioPositionProperty = SimpleIntegerProperty()
    val totalAudioSizeProperty = SimpleIntegerProperty()

    val potentiallyFinishedProperty = chunkTotalProperty.eq(recordedVerses.sizeProperty)
    val potentiallyFinished by potentiallyFinishedProperty

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val listeners = mutableListOf<ListenerDisposer>()
    private val disposables = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        hasVersesProperty.bind(recordedVerses.booleanBinding { it.isNotEmpty() })
        lastRecordedVerseProperty.bind(recordedVerses.integerBinding { it.size })
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
        audioPlayer = narration.getPlayer()
        audioPlayer.addEventListener { event: AudioPlayerEvent ->
            when(event) {
                AudioPlayerEvent.PLAY -> isPlayingProperty.set(true)
                AudioPlayerEvent.COMPLETE, AudioPlayerEvent.PAUSE, AudioPlayerEvent.STOP -> isPlayingProperty.set(false)
                else -> {}
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
                Screen.getMainScreen().width,
                10,
                DEFAULT_SAMPLE_RATE
            )
        )
        totalAudioSizeProperty.set(rendererAudioReader.totalFrames)
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
        renderer.clearActiveRecordingData()
        audioPlayer.pause()

        narration.loadSectionIntoPlayer(verse)

        // audioPlayer.seek(0)
        audioPlayer.play()
    }

    fun playAll() {
        renderer.clearActiveRecordingData()
        audioPlayer.pause()
        narration.loadChapterIntoPlayer()
        // audioPlayer.seek(0)
        audioPlayer.play()
    }

    fun pause() {
        audioPlayer.pause()
    }

    fun recordAgain(verseIndex: Int) {
        rendererAudioReader.seek(audioPlayer.getDurationInFrames())
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
        rendererAudioReader.seek(audioPlayer.getDurationInFrames())

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
        renderer.clearActiveRecordingData()
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
            renderer.clearActiveRecordingData()
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
            totalAudioSizeProperty.set(rendererAudioReader.totalFrames)

            recordedVerses.setAll(it)

            hasUndo = narration.hasUndo()
            hasRedo = narration.hasRedo()

            recordStart = recordedVerses.isEmpty()
            recordResume = recordedVerses.isNotEmpty()
        }.let(disposables::add)
    }

    fun drawWaveform(context: GraphicsContext, canvas: Canvas) {
        if (::renderer.isInitialized) {
            val position = narration.getLocationInFrames()
            runLater {
                audioPositionProperty.set(position)
            }
            renderer.draw(context, canvas, position)
        }
    }

    fun drawVolumebar(context: GraphicsContext, canvas: Canvas) {
        if (::renderer.isInitialized) {
            volumeBar.draw(context, canvas)
        }
    }

    fun scrollAudio(delta: Int) {
        // logger.error("should be seeking to $delta")
        val wasPlaying = audioPlayer.isPlaying()
        audioPlayer.pause()
        narration.loadChapterIntoPlayer()
        audioPlayer.seek(delta)
        narration.scrollAudio(delta)
        rendererAudioReader.seek(delta)
        if (wasPlaying) audioPlayer.play()
    }
}