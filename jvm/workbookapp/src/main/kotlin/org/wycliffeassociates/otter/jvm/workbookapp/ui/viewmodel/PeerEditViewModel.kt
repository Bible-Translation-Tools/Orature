package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.IUndoable
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.chunking.ChunkConfirmAction
import org.wycliffeassociates.otter.common.domain.model.UndoableActionHistory
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.waveform.IWaveformViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import tornadofx.*
import javax.inject.Inject

class PeerEditViewModel : ViewModel(), IWaveformViewModel {
    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()
    val blindDraftViewModel: BlindDraftViewModel by inject()
    val recorderViewModel: RecorderViewModel by inject()

    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty()
    override var timer: AnimationTimer? = null

    val chunkTitleProperty = workbookDataStore.activeChunkTitleBinding()
    val currentChunkProperty = SimpleObjectProperty<Chunk>()
    val chunkConfirmed = SimpleBooleanProperty(false)
    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val isPlayingProperty = SimpleBooleanProperty(false)
    val disposable = CompositeDisposable()

    lateinit var waveform: Observable<Image>
    var slider: Slider? = null
    var subscribeOnWaveformImages: () -> Unit = {}
    var cleanUpWaveform: () -> Unit = {}

    override var sampleRate: Int = 0 // beware of divided by 0
    override var totalFrames: Int = 0 // beware of divided by 0

    private var audioController: AudioPlayerController? = null
    private val newTakeProperty = SimpleObjectProperty<Take>(null)
    private val builder = ObservableWaveformBuilder()
    private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)
    private val width = Screen.getMainScreen().platformWidth
    private val disposableListeners = mutableListOf<ListenerDisposer>()
    private val selectedTakeDisposable = CompositeDisposable()

    private val actionHistory = UndoableActionHistory<IUndoable>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        currentChunkProperty.bindBidirectional(workbookDataStore.activeChunkProperty)
    }

    fun dockPeerEdit() {
        startAnimationTimer()
        subscribeToChunks()

        currentChunkProperty.onChangeAndDoNowWithDisposer {
            it?.let { chunk ->
                subscribeToSelectedTake(chunk)
                val currentStep = translationViewModel.selectedStepProperty.value
                val isConfirmed = chunk.checkingStatus().ordinal >= checkingStatusFromStep(currentStep).ordinal
                chunkConfirmed.set(isConfirmed)
            }
            actionHistory.clear()
        }.also { disposableListeners.add(it) }

        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
    }

    fun undockPeerEdit() {
        stopAnimationTimer()
        sourcePlayerProperty.unbind()
        selectedTakeDisposable.clear()
        disposable.clear()
        disposableListeners.forEach { it.dispose() }
        disposableListeners.clear()
        actionHistory.clear()
    }

    fun refreshChunkList() {
        workbookDataStore.activeChapterProperty.value?.let { chapter ->
            chapter.chunks.value?.let { chunks ->
                translationViewModel.loadChunks(chunks)
            }
        }
    }

    fun toggleAudio() {
        audioController?.toggle()
    }

    fun pause() {
        audioController?.pause()
    }

    fun seek(location: Int) {
        audioController?.seek(location)
    }

    fun confirmChunk() {
        currentChunkProperty.value?.let { chunk ->
            chunkConfirmed.set(true)
            val checkingStatus = checkingStatusFromStep(
                translationViewModel.selectedStepProperty.value
            )
            val take = chunk.audio.getSelectedTake()!!
            take.checkingState
                .take(1)
                .observeOnFx()
                .subscribe { currentChecking ->
                    val op = ChunkConfirmAction(
                        take,
                        checkingStatus,
                        currentChecking
                    )
                    actionHistory.execute(op)
                    onUndoableAction()
                    refreshChunkList()
                }.dispose()
        }
    }

    fun undo() {
        actionHistory.undo()
        refreshChunkList()
        translationViewModel.canRedoProperty.set(true)
        translationViewModel.canUndoProperty.set(actionHistory.canUndo())
    }

    fun redo() {
        actionHistory.redo()
        refreshChunkList()
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(actionHistory.canRedo())
    }

    fun onRecordNew() {
        blindDraftViewModel.newTakeFile()
            .observeOnFx()
            .subscribe { take ->
                newTakeProperty.set(take)
                recorderViewModel.targetFileProperty.set(take.file)
            }
    }

    fun onRecordFinish(result: RecorderViewModel.Result) {
        if (result == RecorderViewModel.Result.SUCCESS) {
            workbookDataStore.chunk?.audio?.insertTake(newTakeProperty.value)
        } else {
            newTakeProperty.value?.file?.delete()
            newTakeProperty.set(null)
        }
    }

    private fun subscribeToChunks() {
        workbookDataStore.chapter
            .chunks
            .observeOnFx()
            .subscribe { chunks ->
                translationViewModel.loadChunks(chunks)
                (chunks.firstOrNull { it.checkingStatus() == CheckingStatus.UNCHECKED } ?: chunks.firstOrNull())
                    ?.let { chunk ->
                        translationViewModel.selectChunk(chunk.sort)
                    }
            }.addTo(disposable)
    }

    private fun subscribeToSelectedTake(chunk: Chunk) {
        selectedTakeDisposable.clear()
        chunk.audio.selected
            .observeOnFx()
            .subscribe {
                it.value?.let { take -> loadTargetAudio(take) }
            }.addTo(selectedTakeDisposable)
    }

    private fun loadTargetAudio(take: Take) {
        val audioPlayer: IAudioPlayer = audioConnectionFactory.getPlayer()
        audioPlayer.load(take.file)
        audioPlayer.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
        }
        waveformAudioPlayerProperty.set(audioPlayer)

        loadAudioController(audioPlayer)

        val audio = OratureAudioFile(take.file)
        createWaveformImages(audio)
        subscribeOnWaveformImages()
    }

    private fun loadAudioController(player: IAudioPlayer) {
        audioController = AudioPlayerController(slider).also { controller ->
            controller.load(player)
            isPlayingProperty.bind(controller.isPlayingProperty)
        }
    }

    private fun createWaveformImages(audio: OratureAudioFile) {
        cleanUpWaveform()
        imageWidthProperty.set(computeImageWidth(width))

        builder.cancel()
        waveform = builder.buildAsync(
            audio.reader(),
            width = imageWidthProperty.value.toInt(),
            height = height,
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        )
    }

    private fun onUndoableAction() {
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(false)
    }

    private fun checkingStatusFromStep(step: ChunkingStep): CheckingStatus {
        return when (step) {
            ChunkingStep.PEER_EDIT -> CheckingStatus.PEER_EDIT
            ChunkingStep.KEYWORD_CHECK -> CheckingStatus.KEYWORD
            ChunkingStep.VERSE_CHECK -> CheckingStatus.VERSE
            else -> CheckingStatus.UNCHECKED
        }
    }
}