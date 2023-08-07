package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.io.File
import javax.inject.Inject

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

    val playingVerseProperty = SimpleObjectProperty<VerseNode?>()
    var playingVerse by playingVerseProperty

    val hasUndoProperty = SimpleBooleanProperty()
    var hasUndo by hasUndoProperty
    val hasRedoProperty = SimpleBooleanProperty()
    var hasRedo by hasRedoProperty

    val potentiallyFinishedProperty = SimpleBooleanProperty()
    var potentiallyFinished by potentiallyFinishedProperty


    val recordedVerses = observableListOf<VerseNode>()
    val hasVersesProperty = SimpleBooleanProperty()
    val lastRecordedVerseProperty = SimpleIntegerProperty()

    val pluginContextProperty = SimpleObjectProperty(PluginType.EDITOR)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    private val listeners = mutableListOf<ListenerDisposer>()
    private val disposables = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
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

        closeNarrationAudio()
    }

    private fun initializeNarration(chapter: Chapter) {
        Completable.fromAction {
            narration = narrationFactory.create(workbookDataStore.workbook, chapter)
        }
            .doOnError {
                logger.error("An error occurred in loadNarration", it)
            }
            .subscribe {
                subscribeActiveVersesChanged()

                recordStart = recordedVerses.isEmpty()
                recordResume = recordedVerses.isNotEmpty()
                potentiallyFinished = checkPotentiallyFinished()
            }
    }

    private fun reloadNarration() {
        Completable.fromCallable {
            narration.loadFromSelectedChapterFile()
        }
            .doOnError {
                logger.error("An error occurred in loadNarration", it)
            }
            .subscribe {
                recordedVerses.setAll(narration.activeVerses)

                recordStart = recordedVerses.isEmpty()
                recordResume = recordedVerses.isNotEmpty()
                potentiallyFinished = checkPotentiallyFinished()
            }
    }

    fun snackBarMessage(message: String) {
        snackBarObservable.onNext(message)
    }

    fun play(verse: VerseNode) {
        if (playingVerse == verse) {
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
        reloadNarration()
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

    private fun checkPotentiallyFinished(): Boolean {
        return workbookDataStore.chapter.chunkCount.blockingGet() == recordedVerses.size
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
        }.let(disposables::add)
    }
}