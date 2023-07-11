package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.narration.*
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.common.domain.narration.AudioFileUtils
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
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
                                    //hide()
                                    fire(PlayVerseEvent(verse))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                            item("") {
                                text = "Record Again"
                                action {
                                    //hide()
                                    fire(RecordAgainEvent(index))
                                }
                                disableWhen {
                                    viewModel.isRecordingProperty
                                }
                            }
                            item("") {
                                text = "Open in..."
                                action {
                                    //hide()
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
    lateinit var audioFileUtils: AudioFileUtils

    private var recorder: IAudioRecorder? = null
    private var writer: WavFileWriter? = null
    private var recordedAudio: AudioFile? = null

    private val recordStartProperty = SimpleBooleanProperty()
    private var recordStart by recordStartProperty

    private val recordPauseProperty = SimpleBooleanProperty()
    private var recordPause by recordPauseProperty

    private val recordResumeProperty = SimpleBooleanProperty()
    private var recordResume by recordResumeProperty

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

    private val narrationHistory = NarrationHistory()
    private lateinit var chapterPcmFile: File

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)

    val recordedVerses = observableListOf<VerseNode>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        narrationViewViewModel.recordStartProperty.bind(recordStartProperty)
        narrationViewViewModel.recordResumeProperty.bind(recordResumeProperty)
        narrationViewViewModel.isRecordingProperty.bind(isRecordingProperty)
        narrationViewViewModel.recordPauseProperty.bind(recordPauseProperty)
        narrationViewViewModel.isRecordingAgainProperty.bind(isRecordingAgainProperty)

        narrationViewViewModel.hasUndoProperty.bind(hasUndoProperty)
        narrationViewViewModel.hasRedoProperty.bind(hasRedoProperty)

        narrationViewViewModel.lastRecordedVerseProperty.bind(recordedVerses.integerBinding { it.size })
    }

    fun onDock() {
        loadVerses()
        initializeRecorder()
    }

    fun onUndock() {
        closeRecorder()
    }

    private fun loadVerses() {
        // Load verses from database and map to recordedVerses list
        // then based on the size of the list set start, resume and potentiallyFinished properties

        recordStart = recordedVerses.isEmpty()
        recordResume = recordedVerses.isNotEmpty()
    }

    fun play(verse: VerseNode) {
        if (playingVerse == verse) {
            audioController.toggle()
        } else {
            audioController.pause()

            player.loadSection(chapterPcmFile, verse.start, verse.end)
            audioController.load(player)
            audioController.seek(0)
            audioController.play()

            playingVerse = verse
        }
    }

    fun recordAgain(verseIndex: Int) {
        stopPlayer()

        recordedAudio?.let {
            val action = RecordAgainAction(recordedVerses, it, verseIndex)
            narrationHistory.execute(action)
            updateHistory()
        }

        recorder?.start()
        writer?.start()

        recordAgainVerseIndex = verseIndex
        isRecording = true
        isRecordingAgain = true
        recordPause = false
    }

    fun openInAudioPlugin(index: Int) {
        recordedAudio?.let { audio ->
            val verse = recordedVerses[index]
            val file = audioFileUtils.getSectionAsFile(audio, verse.start, verse.end)
            processWithEditor(file, index)
        }
    }

    fun onNext() {
        when {
            isRecording -> {
                recordedAudio?.let {
                    recordedVerses.lastOrNull()?.end = it.totalFrames
                    val action = NextVerseAction(recordedVerses, it)
                    narrationHistory.execute(action)
                    updateHistory()
                }
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
        val action = ResetAllAction(recordedVerses)
        narrationHistory.execute(action)
        updateHistory()

        recordStart = true
        recordResume = false
        recordPause = false
    }

    fun undo() {
        narrationHistory.undo()

        recordPause = false
        recordStart = recordedVerses.isEmpty()
        recordResume = recordedVerses.isNotEmpty()

        updateHistory()
    }

    fun redo() {
        narrationHistory.redo()

        recordPause = false
        recordStart = recordedVerses.isEmpty()
        recordResume = recordedVerses.isNotEmpty()

        updateHistory()
    }

    private fun updateHistory() {
        hasUndo = narrationHistory.hasUndo()
        hasRedo = narrationHistory.hasRedo()

        narrationHistory.updateSavedHistoryFile(recordedVerses)
    }

    private fun record() {
        stopPlayer()

        recordedAudio?.let {
            val action = NextVerseAction(recordedVerses, it)
            narrationHistory.execute(action)
            updateHistory()
        }

        recorder?.start()
        writer?.start()

        isRecording = true
        recordStart = false
        recordResume = false
    }

    private fun pauseRecording() {
        isRecording = false
        recordPause = true

        recorder?.pause()
        writer?.pause()

        recordedAudio?.let {
            recordedVerses.lastOrNull()?.end = it.totalFrames
        }

        updateHistory()
    }

    private fun resumeRecording() {
        stopPlayer()

        recorder?.start()
        writer?.start()

        isRecording = true
        recordPause = false
    }

    private fun stopRecordAgain() {
        recordAgainVerseIndex?.let { verseIndex ->
            recorder?.pause()
            writer?.pause()

            recordAgainVerseIndex = null
            isRecording = false
            isRecordingAgain = false
            recordPause = true
            recordedAudio?.let {
                recordedVerses[verseIndex].end = it.totalFrames
            }
            updateHistory()
        }
    }

    private fun stopPlayer() {
        audioController.pause()
        playingVerse = null
    }

    private fun initializeRecorder() {
        recorder = audioConnectionFactory.getRecorder().also { rec ->
            val audioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir

            chapterPcmFile = File(audioDir, "narration.${AudioFileFormat.PCM.extension}")
            narrationHistory.initSavedHistoryFile(audioDir)

            loadWorkInProgress()

            recordedAudio = AudioFile(chapterPcmFile).also {
                writer = WavFileWriter(it, rec.getAudioStream(), true) {  /* no op */  }
            }
        }
    }

    private fun closeRecorder() {
        recorder?.stop()
        recorder = null

        writer?.writer?.dispose()
        writer = null
    }

    private fun processWithEditor(file: File, index: Int) {
        val pluginType = PluginType.EDITOR
        pluginContextProperty.set(pluginType)

        audioPluginViewModel.getPlugin(pluginType)
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType, ${e.message}")
            }
            .flatMapSingle { plugin ->
                workbookDataStore.activeTakeNumberProperty.set(1)
                fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
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
                        recordedAudio?.let { audio ->
                            addEditedVerse(audio, file, index)
                        }
                    }
                }
                fire(PluginClosedEvent(pluginType))
            }
    }

    private fun addEditedVerse(audio: AudioFile, file: File, index: Int) {
        val start = audio.totalFrames
        audioFileUtils.appendFile(audio, file)
        val end = audio.totalFrames
        val action = EditVerseAction(recordedVerses, index, start, end)
        narrationHistory.execute(action)
        updateHistory()
    }

    private fun loadWorkInProgress() {
        if (!chapterPcmFile.exists()) {
            chapterPcmFile.createNewFile()
        }

        val nodes = narrationHistory.loadSavedHistoryFile()
        recordedVerses.addAll(nodes)
    }
}

class RecordAgainEvent(val index: Int) : FXEvent()
class PlayVerseEvent(val verse: VerseNode) : FXEvent()
class OpenInAudioPluginEvent(val index: Int) : FXEvent()