package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javafx.animation.AnimationTimer
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.wav.IWaveFileCreator
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.ChapterTranslationBuilder
import org.wycliffeassociates.otter.common.domain.model.ChunkMarkerModel
import org.wycliffeassociates.otter.common.domain.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.controls.waveform.IMarkerViewModel
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import java.text.MessageFormat
import javax.inject.Inject
import kotlin.collections.sortBy

class ChapterReviewViewModel : ViewModel(), IMarkerViewModel {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var concatenateAudio: ConcatenateAudio

    @Inject
    lateinit var waveFileCreator: IWaveFileCreator

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    @Inject
    lateinit var chapterTranslationBuilder: ChapterTranslationBuilder

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()
    val translationViewModel: TranslationViewModel2 by inject()

    override var markerModel: VerseMarkerModel? = null
    override val markers = observableListOf<ChunkMarkerModel>()

    override val markerCountProperty = markers.sizeProperty
    override val currentMarkerNumberProperty = SimpleIntegerProperty(-1)
    override var resumeAfterScroll: Boolean = false

    /** This property must be initialized before calling dock() */
    override var audioController: AudioPlayerController? = null
    override val waveformAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    override val positionProperty = SimpleDoubleProperty(0.0)
    override var imageWidthProperty = SimpleDoubleProperty(0.0)
    override var timer: AnimationTimer? = null
    override var sampleRate: Int = 0 // beware of divided by 0
    override var totalFrames: Int = 0 // beware of divided by 0

    lateinit var waveform: Observable<Image>
    private val sourceAudio by audioDataStore.sourceAudioProperty
    private val width = Screen.getMainScreen().platformWidth
    private val height = Integer.min(Screen.getMainScreen().platformHeight, 500)
    private val builder = ObservableWaveformBuilder()

    var subscribeOnWaveformImages: () -> Unit = {}
    var cleanUpWaveform: () -> Unit = {}

    val chapterTitleProperty = workbookDataStore.activeChapterTitleBinding()
    val sourcePlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val markerProgressCounterProperty = SimpleStringProperty()
    val totalMarkersProperty = SimpleIntegerProperty(0)
    val markersPlacedCountProperty = SimpleIntegerProperty(0)
    val canGoNextChapterProperty: BooleanBinding = translationViewModel.isLastChapterProperty.not().and(
        markersPlacedCountProperty.isEqualTo(totalMarkersProperty)
    )
    val isPlayingProperty = SimpleBooleanProperty(false)
    val compositeDisposable = CompositeDisposable()


    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun dock() {
        startAnimationTimer()

        sourcePlayerProperty.bind(audioDataStore.sourceAudioPlayerProperty)
        workbookDataStore.activeChunkProperty.set(null)

        Completable
            .fromAction {
                audioDataStore.updateSourceAudio()
                audioDataStore.openSourceAudioPlayer()
            }
            .subscribeOn(Schedulers.io())
            .subscribe()

        markersPlacedCountProperty.bind(markers.sizeProperty)
        markerProgressCounterProperty.bind(
            stringBinding(markersPlacedCountProperty, totalMarkersProperty) {
                MessageFormat.format(
                    messages["marker_placed_ratio"],
                    markersPlacedCountProperty.value ?: 0,
                    totalMarkersProperty.value ?: 0
                )
            }
        )

        loadChapterTake()
    }

    fun undock() {
        pauseAudio()
        waveformAudioPlayerProperty.value?.stop()
        audioDataStore.stopPlayers()
        audioDataStore.closePlayers()
        markerModel
            ?.writeMarkers()
            ?.blockingAwait()

        cleanup()
    }

    override fun placeMarker() {
        super.placeMarker()
        onUndoableAction()
    }

    override fun deleteMarker(id: Int) {
        super.deleteMarker(id)
        onUndoableAction()
    }

    override fun moveMarker(id: Int, start: Int, end: Int) {
        super.moveMarker(id, start, end)
        onUndoableAction()
    }

    override fun undoMarker() {
        super.undoMarker()
        val dirty = markerModel?.hasDirtyMarkers() ?: false
        translationViewModel.canUndoProperty.set(dirty)
        translationViewModel.canRedoProperty.set(true)
    }

    override fun redoMarker() {
        super.redoMarker()
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(markerModel?.canRedo() == true)
    }

    fun pauseAudio() = audioController?.pause()

    fun invalidateChapterTake() {
        workbookDataStore.chapter
            .audio
            .getSelectedTake()
            ?.let {
                it.checkingState.accept(
                    TakeCheckingState(CheckingStatus.UNCHECKED, null)
                )
                it.deletedTimestamp.accept(DateHolder.now())
            }
    }

    private fun loadChapterTake() {
        chapterTranslationBuilder
            .getOrCompile(
                workbookDataStore.workbook,
                workbookDataStore.chapter
            )
            .flatMap { take ->
                loadTargetAudio(take)
            }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe { audio ->
                loadVerseMarkers(audio)
                createWaveformImages(audio)
                subscribeOnWaveformImages()
            }
    }

    private fun loadTargetAudio(take: Take) : Single<OratureAudioFile> {
        return Single
            .fromCallable {
                val audioPlayer: IAudioPlayer = audioConnectionFactory.getPlayer()
                audioPlayer.load(take.file)
                audioPlayer.getAudioReader()?.let {
                    sampleRate = it.sampleRate
                    totalFrames = it.totalFrames
                }
                audioController?.let { controller ->
                    controller.load(audioPlayer)
                    isPlayingProperty.bind(controller.isPlayingProperty)
                }
                waveformAudioPlayerProperty.set(audioPlayer)
                OratureAudioFile(take.file)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun loadVerseMarkers(audio: OratureAudioFile) {
        markers.clear()
        val sourceAudio = OratureAudioFile(sourceAudio.file)
        val sourceMarkers = sourceAudio.getMarker<VerseMarker>()
        val markerList = audio.getMarker<VerseMarker>().map {
            ChunkMarkerModel(AudioCue(it.location, it.label))
        }

        totalMarkersProperty.set(sourceMarkers.size)
        markerModel = VerseMarkerModel(
            audio,
            sourceMarkers.size,
            sourceMarkers.map { it.label }
        ).also {
            it.loadMarkers(markerList)
        }
        markers.setAll(markerList)
        markers.sortBy { it.frame }
    }

    private fun cleanup() {
        builder.cancel()
        compositeDisposable.clear()
        stopAnimationTimer()
        markerModel = null
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

    private fun onUndoableAction() {
        translationViewModel.canUndoProperty.set(true)
        translationViewModel.canRedoProperty.set(false)
    }
}
