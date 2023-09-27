package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.ViewModel
import tornadofx.observableListOf
import tornadofx.sizeProperty
import java.io.File
import javax.inject.Inject
import kotlin.math.max

class ConsumeViewModel : ViewModel(), IMarkerViewModel {

    var timer: AnimationTimer? = null

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    lateinit var audio: OratureAudioFile
    lateinit var waveform: Observable<Image>
    var subscribeOnWaveformImages: () -> Unit = {}

    override var markerModel: VerseMarkerModel? = null
    override val markers = observableListOf<ChunkMarkerModel>()
    override val markerCountProperty = markers.sizeProperty
    override val currentMarkerNumberProperty = SimpleIntegerProperty(-1)

    override var audioController: AudioPlayerController? = null
    override val audioPlayer = SimpleObjectProperty<IAudioPlayer>()

    override val positionProperty = SimpleDoubleProperty(0.0)
    override var resumeAfterScroll: Boolean = false
    override var imageWidthProperty = SimpleDoubleProperty(0.0)

    val compositeDisposable = CompositeDisposable()
    val isPlayingProperty = SimpleBooleanProperty(false)

    private var sampleRate: Int = 0 // beware of divided by 0
    private var sourceTotalFrames: Int = 0 // beware of divided by 0

    private val builder = ObservableWaveformBuilder()
    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDockConsume() {
        val wb = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter
        val sourceAudio = wb.sourceAudioAccessor.getChapter(chapter.sort, wb.target)
        audioDataStore.sourceAudioProperty.set(sourceAudio)

        sourceAudio?.file?.let {
            (app as IDependencyGraphProvider).dependencyGraph.inject(this)
            audio = loadAudio(it)
            createWaveformImages(audio)
            subscribeOnWaveformImages()
            loadSourceMarkers(audio)
        }
        startAnimationTimer()
        translationViewModel.currentMarkerProperty.bind(currentMarkerNumberProperty)
    }

    fun onUndockConsume() {
        pause()
        cleanup()
        translationViewModel.currentMarkerProperty.unbind()
        translationViewModel.currentMarkerProperty.set(-1)
    }

    fun initializeAudioController(slider: Slider? = null) {
        audioController = AudioPlayerController(slider)
        audioPlayer.value?.let {
            audioController!!.load(it)
            isPlayingProperty.bind(audioController!!.isPlayingProperty)
        }
    }

    fun pause() {
        audioController?.pause()
    }


    fun loadSourceMarkers(audio: OratureAudioFile) {
        audio.clearCues()
        val verseMarkers = audio.getMarker<VerseMarker>()
        markerModel = VerseMarkerModel(audio, verseMarkers.size, verseMarkers.map { it.label })
        markerModel?.let { markerModel ->
            markers.setAll(markerModel.markers)
        }
    }

    fun loadAudio(audioFile: File): OratureAudioFile {
        val player = audioConnectionFactory.getPlayer()
        val audio = OratureAudioFile(audioFile)
        player.load(audioFile)
        player.getAudioReader()?.let {
            sampleRate = it.sampleRate
            sourceTotalFrames = it.totalFrames
        }
        audioPlayer.set(player)
        return audio
    }

    fun createWaveformImages(audio: OratureAudioFile) {
        imageWidthProperty.set(computeImageWidth(SECONDS_ON_SCREEN))

        waveform = builder.buildAsync(
            audio.reader(),
            width = imageWidthProperty.value.toInt(),
            height = height,
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
        )
    }

    fun cleanup() {
        builder.cancel()
        compositeDisposable.clear()
        stopAnimationTimer()
        markerModel = null
    }

    fun computeImageWidth(secondsOnScreen: Int): Double {
        val samplesPerScreenWidth = audioPlayer.get().getAudioReader()!!.sampleRate * secondsOnScreen
        val samplesPerPixel = samplesPerScreenWidth / width
        val pixelsInDuration = audioPlayer.get().getDurationInFrames() / samplesPerPixel
        return pixelsInDuration.toDouble()
    }

    fun pixelsInHighlight(controlWidth: Double): Double {
        if (sampleRate == 0 || sourceTotalFrames == 0) {
            return 1.0
        }

        val framesInHighlight = sampleRate * SECONDS_ON_SCREEN
        val framesPerPixel = sourceTotalFrames / max(controlWidth, 1.0)
        return max(framesInHighlight / framesPerPixel, 1.0)
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
}