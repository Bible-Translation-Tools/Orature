package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.narration.NarrationHistory
import org.wycliffeassociates.otter.common.data.narration.NextVerseAction
import org.wycliffeassociates.otter.common.data.narration.RecordAgainAction
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.NextVerseEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.RecordVerseEvent
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
                                    hide()
                                    fire(PlayVerseEvent(verse))
                                }
                            }
                            item("") {
                                text = "Record Again"
                                action {
                                    hide()
                                    fire(RecordAgainEvent(index))
                                }
                                disableWhen {
                                    viewModel.isRecordingAgainProperty.or(viewModel.isRecordingProperty)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()

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

        viewModel.onDock()
    }

    override fun onUndock() {
        super.onUndock()

        unsubscribe<RecordVerseEvent> {  }
        unsubscribe<NextVerseEvent> {  }

        viewModel.onUndock()
    }
}

class NarrationBodyViewModel : ViewModel() {

    private val workbookDataStore: WorkbookDataStore by inject()
    private val narrationViewViewModel: NarrationViewViewModel by inject()

    @Inject
    lateinit var player: IAudioPlayer
    private val audioController = AudioPlayerController(Slider())

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

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

    private val narrationHistory = NarrationHistory()
    private lateinit var pcmFile: File

    val recordedVerses = observableListOf<VerseNode>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        narrationViewViewModel.recordStartProperty.bind(recordStartProperty)
        narrationViewViewModel.recordResumeProperty.bind(recordResumeProperty)
        narrationViewViewModel.isRecordingProperty.bind(isRecordingProperty)
        narrationViewViewModel.recordPauseProperty.bind(recordPauseProperty)
        narrationViewViewModel.isRecordingAgainProperty.bind(isRecordingAgainProperty)
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

            player.loadSection(pcmFile, verse.start, verse.end)
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
        }

        recorder?.start()
        writer?.start()

        recordAgainVerseIndex = verseIndex
        isRecordingAgain = true
        recordPause = false
    }

    fun onNext() {
        when {
            isRecording -> {
                recordedAudio?.let {
                    recordedVerses.lastOrNull()?.end = it.totalFrames
                    val action = NextVerseAction(recordedVerses, it)
                    narrationHistory.execute(action)
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
            recordStart || recordResume -> record()
            isRecording -> pauseRecording()
            recordPause -> resumeRecording()
            isRecordingAgain -> stopRecordAgain()
            else -> {}
        }
    }

    private fun record() {
        stopPlayer()

        recordedAudio?.let {
            val action = NextVerseAction(recordedVerses, it)
            narrationHistory.execute(action)
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
            recordAgainVerseIndex = null
            isRecordingAgain = false
            recordPause = true
            recordedAudio?.let {
                recordedVerses[verseIndex].end = it.totalFrames
            }
        }
    }

    private fun stopPlayer() {
        audioController.pause()
        playingVerse = null
    }

    private fun initializeRecorder() {
        recorder = audioConnectionFactory.getRecorder().also { rec ->
            val audioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir
            pcmFile = File.createTempFile("narration", ".${AudioFileFormat.PCM.extension}", audioDir)

            println(pcmFile)

            recordedAudio = AudioFile(pcmFile).also {
                writer = WavFileWriter(it, rec.getAudioStream()) {  /* no op */  }
            }
        }
    }

    private fun closeRecorder() {
        recorder?.stop()
        recorder = null

        writer?.writer?.dispose()
        writer = null
    }
}

class RecordAgainEvent(val index: Int) : FXEvent()
class PlayVerseEvent(val verse: VerseNode) : FXEvent()