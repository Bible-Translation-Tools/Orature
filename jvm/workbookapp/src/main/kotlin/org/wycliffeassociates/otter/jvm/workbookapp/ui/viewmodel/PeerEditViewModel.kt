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
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.waveform.IWaveformViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
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
    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()

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
    private val chunkDisposable = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        currentChunkProperty.bindBidirectional(workbookDataStore.activeChunkProperty)
    }

    fun dockPeerEdit() {
        startAnimationTimer()

        workbookDataStore.chapter
            .chunks
            .observeOnFx()
            .subscribe { chunks ->
                translationViewModel.loadChunks(chunks)
                (chunks.firstOrNull { it.checkingStatus() == CheckingStatus.UNCHECKED } ?: chunks.firstOrNull())
                    ?.let { chunk ->
                        translationViewModel.selectChunk(chunk.sort)
                    }
            }.addTo(compositeDisposable)

        currentChunkProperty.onChangeAndDoNowWithDisposer {
            it?.let { chunk ->
                subscribeToSelectedTake(chunk)
            }
        }.also { disposableListeners.add(it) }

        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
    }

    fun undockPeerEdit() {
        sourcePlayerProperty.unbind()
        compositeDisposable.clear()
        stopAnimationTimer()
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
            val checkingStatus = when (translationViewModel.selectedStepProperty.value) {
                ChunkingStep.PEER_EDIT -> CheckingStatus.PEER_EDIT
                ChunkingStep.KEYWORD_CHECK -> CheckingStatus.KEYWORD
                ChunkingStep.VERSE_CHECK -> CheckingStatus.VERSE
                else -> CheckingStatus.UNCHECKED
            }
            val take = chunk.audio.selected.value?.value
            val checkingStage = TakeCheckingState(checkingStatus, take?.checksum())
            take?.checkingState?.accept(checkingStage)
            refreshChunkList()
        }
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

    private fun subscribeToSelectedTake(chunk: Chunk) {
        chunkDisposable.clear()
        chunk.audio.selected
            .observeOnFx()
            .subscribe {
                it.value?.let { take -> loadTargetAudio(take) }
            }.addTo(chunkDisposable)
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
}