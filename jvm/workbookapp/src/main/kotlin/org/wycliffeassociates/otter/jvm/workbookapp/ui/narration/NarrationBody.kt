package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NextVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.RecordVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationRedoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationResetChapterEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu.NarrationUndoEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import javax.inject.Inject

class NarrationBody : View() {
    private val viewModel: NarrationBodyViewModel by inject()

    override val root = hbox {
        stackpane {
            scrollpane {
                hbox {
                    spacing = 10.0
                    paddingHorizontal = 10.0

                    bindChildren(viewModel.recordedVerses) { verse ->
                        val index = viewModel.recordedVerses.indexOf(verse)
                        val label = (index + 1).toString()

                        menubutton(label) {
                            item("") {
                                text = "Play"
                                action {
                                    fire(PlayVerseEvent(verse))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                            item("") {
                                text = "Record Again"
                                action {
                                    fire(RecordAgainEvent(index))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                            item("") {
                                text = "Open in..."
                                action {
                                    fire(OpenInAudioPluginEvent(index))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        subscribe<NarrationUndoEvent> {
            viewModel.undo()
        }

        subscribe<NarrationRedoEvent> {
            viewModel.redo()
        }

        subscribe<NarrationResetChapterEvent> {
            viewModel.resetChapter()
        }

        subscribe<RecordVerseEvent> {
            viewModel.toggleRecording()
        }

        subscribe<NextVerseEvent> {
            viewModel.onNext()
        }

        subscribe<PlayVerseEvent> {
            viewModel.play(it.verse)
        }

        subscribe<RecordAgainEvent> {
            viewModel.recordAgain(it.index)
        }

        subscribe<OpenInAudioPluginEvent> {
            viewModel.openInAudioPlugin(it.index)
        }

        subscribe<ChapterLoadEvent> {
            viewModel.onChapterLoad(it.chapter, it.status)
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.onDock()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.onUndock()
    }
}

class NarrationBodyViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(NarrationBodyViewModel::class.java)

    private val workbookDataStore: WorkbookDataStore by inject()
    private val narrationViewViewModel: NarrationViewViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    @Inject
    lateinit var player: IAudioPlayer
    private val audioController = AudioPlayerController(Slider())

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    private val recordStartProperty = SimpleBooleanProperty()
    private var recordStart by recordStartProperty

    private val recordPauseProperty = SimpleBooleanProperty()
    private var recordPause by recordPauseProperty

    private val recordResumeProperty = SimpleBooleanProperty()
    private var recordResume by recordResumeProperty

    private val potentiallyFinishedProperty = SimpleBooleanProperty()
    private var potentiallyFinished by potentiallyFinishedProperty

    val isRecordingProperty = SimpleBooleanProperty()
    private var isRecording by isRecordingProperty

    val isRecordingAgainProperty = SimpleBooleanProperty()
    private var isRecordingAgain by isRecordingAgainProperty

    private val recordAgainVerseIndexProperty = SimpleObjectProperty<Int?>()
    private var recordAgainVerseIndex by recordAgainVerseIndexProperty

    private val playingVerseProperty = SimpleObjectProperty<VerseNode?>()
    private var playingVerse by playingVerseProperty

    private val hasUndoProperty = SimpleBooleanProperty()
    private var hasUndo by hasUndoProperty

    private val hasRedoProperty = SimpleBooleanProperty()
    private var hasRedo by hasRedoProperty

    private lateinit var narration: Narration

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)
    val chapterOpenInPluginProperty = SimpleBooleanProperty()

    val recordedVerses = observableListOf<VerseNode>()

    private val listeners = mutableListOf<ListenerDisposer>()
    private val disposables = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        narrationViewViewModel.recordStartProperty.bind(recordStartProperty)
        narrationViewViewModel.recordResumeProperty.bind(recordResumeProperty)
        narrationViewViewModel.isRecordingProperty.bind(isRecordingProperty)
        narrationViewViewModel.recordPauseProperty.bind(recordPauseProperty)
        narrationViewViewModel.isRecordingAgainProperty.bind(isRecordingAgainProperty)

        narrationViewViewModel.hasUndoProperty.bind(hasUndoProperty)
        narrationViewViewModel.hasRedoProperty.bind(hasRedoProperty)
        narrationViewViewModel.hasVersesProperty.bind(recordedVerses.booleanBinding { it.isNotEmpty() })

        narrationViewViewModel.lastRecordedVerseProperty.bind(recordedVerses.integerBinding { it.size })
    }

    fun onDock() {
        workbookDataStore.activeChapterProperty.onChangeAndDoNowWithDisposer {
            it?.let { chapter ->
                initializeNarration(chapter)
            }
        }.let(listeners::add)
    }

    fun onUndock() {
        listeners.forEach(ListenerDisposer::dispose)
        disposables.dispose()

        closeRecorder()
    }

    fun play(verse: VerseNode) {
        if (playingVerse == verse) {
            audioController.toggle()
        } else {
            audioController.pause()

            narration.loadSectionIntoPlayer(verse)

            audioController.load(player)
            audioController.seek(0)
            audioController.play()

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

    fun onChapterLoad(chapter: Chapter, status: ChapterLoadStatus) {
        when (status) {
            ChapterLoadStatus.STARTED -> {
                chapterOpenInPluginProperty.value = true
                initializeNarration(chapter)
            }
            ChapterLoadStatus.FINISHED -> chapterOpenInPluginProperty.value = false
        }
    }

    fun onNext() {
        when {
            isRecording -> {
                narration.finalizeVerse()
                narration.onNewVerse()
            }
            recordPause -> {
                recordPause = false
                recordResume = true
            }
            else -> {}
        }
    }

    fun toggleRecording() {
        when {
            isRecording && !isRecordingAgain -> pauseRecording()
            isRecording && isRecordingAgain -> stopRecordAgain()
            recordPause -> resumeRecording()
            recordStart || recordResume -> record()
            else -> {}
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

    private fun initializeNarration(chapter: Chapter) {
        Completable.fromAction {
            narration = Narration.load(
                workbookDataStore.workbook,
                chapter,
                directoryProvider,
                workbookDataStore.workbook.projectFilesAccessor,
                audioConnectionFactory.getRecorder(),
                player,
                chapterOpenInPluginProperty.value
            )
        }
            .doOnError {
                logger.error("An error occurred in loadNarration", it)
            }
            .subscribe {
                subscribeActiveVersesChanged()
                recordedVerses.setAll(narration.activeVerses)

                recordStart = recordedVerses.isEmpty()
                recordResume = recordedVerses.isNotEmpty()
                potentiallyFinished = checkPotentiallyFinished()

                FX.eventbus.fire(ChapterLoadEvent(chapter, ChapterLoadStatus.FINISHED))
            }
    }

    private fun record() {
        stopPlayer()

        narration.onNewVerse()

        isRecording = true
        recordStart = false
        recordResume = false
    }

    private fun pauseRecording() {
        isRecording = false
        recordPause = true

        narration.pauseRecording()
        narration.finalizeVerse()

        potentiallyFinished = checkPotentiallyFinished()
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
        audioController.pause()
        playingVerse = null
    }

    private fun closeRecorder() {
        narration.closeRecorder()
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

    private fun checkPotentiallyFinished(): Boolean {
        return workbookDataStore.chapter.chunkCount.blockingGet() == recordedVerses.size
    }

    private fun subscribeActiveVersesChanged() {
        narration.onActiveVersesUpdated.subscribe {
            recordedVerses.setAll(it)

            hasUndo = narration.hasUndo()
            hasRedo = narration.hasRedo()

            recordStart = recordedVerses.isEmpty()
            recordResume = recordedVerses.isNotEmpty()
        }.let(disposables::add)
    }
}

class RecordAgainEvent(val index: Int) : FXEvent()
class PlayVerseEvent(val verse: VerseNode) : FXEvent()
class OpenInAudioPluginEvent(val index: Int) : FXEvent()

class ChapterLoadEvent(val chapter: Chapter, val status: ChapterLoadStatus): FXEvent()

enum class ChapterLoadStatus {
    STARTED,
    FINISHED
}