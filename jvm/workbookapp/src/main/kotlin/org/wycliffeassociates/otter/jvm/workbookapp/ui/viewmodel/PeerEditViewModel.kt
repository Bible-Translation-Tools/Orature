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
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import javax.inject.Inject
import kotlin.math.max

class PeerEditViewModel : ViewModel() {
    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()

    val chunkTitleProperty = workbookDataStore.activeChunkTitleBinding()
    val currentChunkProperty = SimpleObjectProperty<Chunk>()
    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val targetPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val positionProperty = SimpleDoubleProperty(0.0)
    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()

    lateinit var waveform: Observable<Image>
    var timer: AnimationTimer? = null
    var subscribeOnWaveformImages: () -> Unit = {}
    var cleanUpWaveform: () -> Unit = {}

    private var sampleRate: Int = 0 // beware of divided by 0
    private var targetTotalFrames: Int = 0 // beware of divided by 0

    private var audioController: AudioPlayerController? = null
    private val builder = ObservableWaveformBuilder()
    private var imageWidthProperty = SimpleDoubleProperty(0.0)
    private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)
    private val width = Screen.getMainScreen().platformWidth
    private val disposableListeners = mutableListOf<ListenerDisposer>()
    private val chunkDisposable = CompositeDisposable()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        currentChunkProperty.bindBidirectional(workbookDataStore.activeChunkProperty)
    }

    fun dockPeerEdit() {
        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        startAnimationTimer()
        currentChunkProperty.onChangeAndDoNowWithDisposer {
            it?.let { chunk ->
                subscribeToSelectedTake(chunk)
            }
        }.also { disposableListeners.add(it) }
    }

    fun undockPeerEdit() {
        sourcePlayerProperty.unbind()
        compositeDisposable.clear()
        stopAnimationTimer()
    }

    fun initializeAudioController(slider: Slider? = null) {
        audioController = AudioPlayerController(slider).also { controller ->
            targetPlayerProperty.value?.let {
                controller.load(it)
                isPlayingProperty.bind(controller.isPlayingProperty)
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

    fun pixelsInHighlight(controlWidth: Double): Double {
        if (sampleRate == 0 || targetTotalFrames == 0) {
            return 1.0
        }

        val framesInHighlight = sampleRate * SECONDS_ON_SCREEN
        val framesPerPixel = targetTotalFrames / max(controlWidth, 1.0)
        return max(framesInHighlight / framesPerPixel, 1.0)
    }

    fun getLocationInFrames(): Int {
        return targetPlayerProperty.value?.getLocationInFrames() ?: 0
    }

    fun getDurationInFrames(): Int {
        return targetPlayerProperty.value?.getDurationInFrames() ?: 0
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
            targetTotalFrames = it.totalFrames
        }
        targetPlayerProperty.set(audioPlayer)

        initializeAudioController()

        val audio = OratureAudioFile(take.file)
        createWaveformImages(audio)
        subscribeOnWaveformImages()
    }

    private fun createWaveformImages(audio: OratureAudioFile) {
        cleanUpWaveform()
        imageWidthProperty.set(computeImageWidth(targetPlayerProperty.value))

        builder.cancel()
        waveform = builder.buildAsync(
            audio.reader(),
            width = imageWidthProperty.value.toInt(),
            height = height,
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        )
    }

    private fun computeImageWidth(audioPlayer: IAudioPlayer, secondsOnScreen: Int = SECONDS_ON_SCREEN): Double {
        val samplesPerScreenWidth = sampleRate * secondsOnScreen
        val samplesPerPixel = samplesPerScreenWidth / width
        val pixelsInDuration = audioPlayer.getDurationInFrames() / samplesPerPixel
        return pixelsInDuration.toDouble()
    }

    private fun startAnimationTimer() {
        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                calculatePosition()
            }
        }.apply { start() }
    }

    private fun stopAnimationTimer() {
        timer?.stop()
        timer = null
    }

    private fun calculatePosition() {
        targetPlayerProperty.value?.let { audioPlayer ->
            val current = audioPlayer.getLocationInFrames()
            val duration = audioPlayer.getDurationInFrames().toDouble()
            val percentPlayed = current / duration
            val pos = percentPlayed * imageWidthProperty.value
            positionProperty.set(pos)
        }
    }
}