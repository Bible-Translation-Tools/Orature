package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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
import org.wycliffeassociates.otter.common.domain.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.common.domain.model.VerseMarkerModel
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.ViewModel
import tornadofx.observableListOf
import tornadofx.sizeProperty
import java.io.File
import javax.inject.Inject

class ConsumeViewModel : ViewModel(), IMarkerViewModel {

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
    override var resumeAfterScroll: Boolean = false

    /** This property must be initialized before calling dock() */
    override var audioController: AudioPlayerController? = null
    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    override var sampleRate: Int = 0 // beware of divided by 0
    override var totalFrames: Int = 0 // beware of divided by 0
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty(0.0)
    override val audioPositionProperty = SimpleIntegerProperty()

    val compositeDisposable = CompositeDisposable()
    val isPlayingProperty = SimpleBooleanProperty(false)

    val totalFramesProperty = SimpleIntegerProperty()

    private val builder = ObservableWaveformBuilder()
    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDockConsume() {
        val wb = workbookDataStore.workbook
        val chapter = workbookDataStore.chapter
        Maybe
            .fromCallable<SourceAudio> {
                wb.sourceAudioAccessor.getChapter(chapter.sort, wb.target)
            }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { sa ->
                audioDataStore.sourceAudioProperty.set(sa)
                audio = loadAudio(sa.file)
                createWaveformImages(audio)
                subscribeOnWaveformImages()
                loadSourceMarkers(audio)
            }

        translationViewModel.currentMarkerProperty.bind(currentMarkerNumberProperty)
    }

    fun onUndockConsume() {
        pause()
        cleanup()
        translationViewModel.currentMarkerProperty.unbind()
        translationViewModel.currentMarkerProperty.set(-1)
    }

    fun pause() {
        audioController?.pause()
    }

    private fun loadSourceMarkers(audio: OratureAudioFile) {
        audio.clearCues()
        val verseMarkers = audio.getMarker<VerseMarker>()
        markerModel = VerseMarkerModel(audio, verseMarkers.size, verseMarkers.map { it.label })
        markerModel?.let { markerModel ->
            markers.setAll(markerModel.markers)
        }
    }


    private fun createWaveformImages(audio: OratureAudioFile) {
        imageWidthProperty.set(computeImageWidth(width, SECONDS_ON_SCREEN))

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
        markerModel = null
    }

    private fun loadAudio(audioFile: File): OratureAudioFile {
        val player = audioConnectionFactory.getPlayer()
        val audio = OratureAudioFile(audioFile)
        player.load(audioFile)
        player.getAudioReader()?.let {
            sampleRate = it.sampleRate
            totalFrames = it.totalFrames
            totalFramesProperty.set(totalFrames)
        }

        waveformAudioPlayerProperty.set(player)
        audioController?.let {
            it.load(player)
            isPlayingProperty.bind(it.isPlayingProperty)
        }

        return audio
    }
}