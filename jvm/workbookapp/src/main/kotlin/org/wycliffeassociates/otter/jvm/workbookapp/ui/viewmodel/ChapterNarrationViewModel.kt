package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.narration.ImportChunks
import org.wycliffeassociates.otter.common.domain.narration.SplitAudioOnCues
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.recorder.Drawable
import org.wycliffeassociates.otter.jvm.controls.recorder.VolumeBar
import org.wycliffeassociates.otter.jvm.controls.recorder.WaveformLayer
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.*
import java.io.File
import javax.inject.Inject

private const val WAV_COLOR = "#015AD990"
private const val BACKGROUND_COLOR = "#FFFFFF00"

private const val INVERTED_WAV_COLOR = "#F2F5F3FF"
private const val INVERTED_BACKGROUND_COLOR = "#015AD9FF"

class ChapterNarrationViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ChapterPageViewModel::class.java)

    @Inject
    lateinit var player: IAudioPlayer
    private val audioController = AudioPlayerController(Slider())

    @Inject
    lateinit var splitAudioOnCues: SplitAudioOnCues
    @Inject
    lateinit var importChunks: ImportChunks

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory
    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    val workbookDataStore: WorkbookDataStore by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    val allChunks: ObservableList<ChunkData> = FXCollections.observableArrayList()
    val allSortedChunks = SortedList(allChunks, compareBy { it.sort })
    val recordedSortedChunks = SortedList(allChunks, compareByDescending<ChunkData>  { it.sort })
    val recordedChunks = FilteredList(recordedSortedChunks) { it.hasAudio() }

    val currentVerseLabelProperty = SimpleStringProperty()
    val floatingCardVisibleProperty = SimpleBooleanProperty()
    val onCurrentVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val initialSelectedItemProperty = SimpleObjectProperty<ChunkData>()

    var onWaveformClicked: (ChunkData) -> Unit = {}
    var onPlaybackStarted: (ChunkData) -> Unit = {}

    var isRecordingProperty = SimpleBooleanProperty(false)
    var isRecording by isRecordingProperty

    val waveformDrawableProperty = SimpleObjectProperty<Drawable>()
    val volumebarDrawableProperty = SimpleObjectProperty<Drawable>()

    private val playingChunkProperty = SimpleObjectProperty<ChunkData>()
    val recordingChunkProperty = SimpleObjectProperty<ChunkData>()
    private var loading: Boolean by property(false)
    private val loadingProperty = getProperty(ChapterNarrationViewModel::loading)

    private var allChunksLoaded: Boolean by property(false)
    private val allChunksLoadedProperty = getProperty(ChapterNarrationViewModel::allChunksLoaded)

    private val disposables = CompositeDisposable()
    private val listeners = mutableListOf<ListenerDisposer>()

    private val recorder: IAudioRecorder
    private var writer: WavFileWriter? = null
    private var renderer: ActiveRecordingRenderer? = null

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        recorder = audioConnectionFactory.getRecorder()
    }

    fun dock() {
        allChunksLoadedProperty.onChangeWithDisposer { loaded ->
            if (loaded == true) {
                if (recordedChunks.isNotEmpty()) {
                    initialSelectedItemProperty.set(recordedChunks.last())
                } else {
                    initialSelectedItemProperty.set(allChunks.first())
                }
            }
        }.let { listeners.add(it) }

        workbookDataStore.activeChapterProperty.value?.let { chapter ->
            val totalChunks = chapter.chunkCount.blockingGet()
            allChunks.onChangedObservable().subscribe {
                if (totalChunks == it.size) {
                    allChunksLoaded = true
                    loading = false
                } else {
                    allChunksLoaded = false
                }
            }.also(disposables::add)

            splitChapter(chapter)
                .andThen(loadChunks(chapter))
                .subscribe()
                .also(disposables::add)
        }
    }

    fun undock() {
        workbookDataStore.selectedChapterPlayerProperty.set(null)
        initialSelectedItemProperty.set(null)
        allChunksLoaded = false

        closePlayer()
        allChunks.clear()
        disposables.clear()
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()

        playingChunkProperty.set(null)
        recordingChunkProperty.value?.let {
            completeRecording(it)
        }
        recordingChunkProperty.set(null)
        recordedChunks.setPredicate { it.hasAudio() }
    }

    private fun onPlay(chunk: ChunkData) {
        if (playingChunkProperty.value == chunk) {
            audioController.toggle()
        } else {
            playingChunkProperty.value?.let {
                audioController.pause()
                audioController.seek(0)
                it.isPlayingProperty.unbind()
                it.playbackPositionProperty.unbind()
            }

            chunk.file?.let {
                onPlaybackStarted(chunk)

                player.load(it)
                audioController.load(player)
                audioController.play()

                playingChunkProperty.set(chunk)
                chunk.isPlayingProperty.bind(audioController.isPlayingProperty)
                audioController.audioSlider?.let { slider ->
                    chunk.playbackPositionProperty.bind(slider.valueProperty().map(Number::toInt))
                }
                chunk.totalFrames = player.getDurationInFrames()
            }
        }
    }

    private fun onChunkOpenIn(chunk: ChunkData) {
        println("Opening verse ${chunk.title} in external app...")
    }

    private fun onChunkRecord(chunk: ChunkData) {
        println("Recording verse ${chunk.title}")
    }

    private fun onRecordChunkAgain(chunk: ChunkData) {
        // Make it `it.sort < chunk.sort` to hide the chunk
        // that is currently being recorded,
        // but you won't be able to stop recording
        recordedChunks.setPredicate { it.sort <= chunk.sort }
        playingChunkProperty.set(null)

        if (recordingChunkProperty.value == chunk) {
            completeRecording(chunk)
            recordedChunks.setPredicate { it.hasAudio() }
        } else {
            recordingChunkProperty.value?.let {
                completeRecording(it)
            }

            recordingChunkProperty.set(chunk)
            isRecording = true

            initializeRecordingAudio(chunk.file!!)

            recorder.start()
            writer?.start()
        }
    }

    private fun closePlayer() {
        player.stop()
        player.release()
        audioController.release()
    }

    private fun loadChunks(chapter: Chapter): Observable<ChunkData> {
        loading = true
        return chapter.chunks
            .doOnError { e ->
                logger.error("Error in loading chapter chunks: $chapter", e)
            }
            .observeOnFx()
            .flatMap {
                val data = ChunkData(it)
                data.onPlay = ::onPlay
                data.onOpenApp = ::onChunkOpenIn
                data.onRecordAgain = ::onRecordChunkAgain
                data.onWaveformClicked = { chunk ->
                    onWaveformClicked(chunk)
                }
                data.onRecord = ::onChunkRecord

                loadChunkMedia(it.audio.selected.value?.value?.file, data)
            }
            .observeOnFx()
            .map {
                it.imageLoading = false
                allChunks.add(it)
                it
            }
    }

    private fun splitChapter(chapter: Chapter): Completable {
        return chapter.audio.selected.value?.value?.file?.let {
            splitAudioOnCues.execute(it)
                .flatMapCompletable { chunkFiles ->
                    val chunks = chapter.chunks.getValues(emptyArray())
                    importChunks.execute(
                        workbookDataStore.workbook,
                        chapter,
                        workbookDataStore.activeResourceMetadata,
                        workbookDataStore.activeProjectFilesAccessor,
                        chunkFiles,
                        chunks
                    )
                }
        } ?: Completable.complete()
    }

    private fun loadChunkMedia(audioFile: File?, chunkData: ChunkData): Observable<ChunkData> {
        return audioFile?.let {
            chunkData.file = audioFile
            chunkData.imageLoading = true

            createWaveformImage(audioFile, WAV_COLOR, BACKGROUND_COLOR)
                .flatMap {
                    chunkData.image = it
                    createWaveformImage(audioFile, INVERTED_WAV_COLOR, INVERTED_BACKGROUND_COLOR)
                }.flatMapObservable {
                    chunkData.invertedImage = it
                    Observable.just(chunkData)
                }
        } ?: run {
            Observable.just(chunkData)
        }
    }

    private fun createWaveformImage(file: File, color: String, background: String): Single<Image> {
        val audio = AudioFile(file)
        val reader = audio.reader()
        val width = (audio.reader().totalFrames / DEFAULT_SAMPLE_RATE) * 100

        return ObservableWaveformBuilder()
            .build(
                reader = reader,
                width = width,
                height = 120,
                wavColor = Color.web(color),
                background = Color.web(background)
            )
    }

    private fun pauseRecording() {
        writer?.pause()
        recorder.pause()
        renderer?.clearData()
        writer?.let {
            renderer?.setRecordingStatusObservable(it.isWriting)
        }
        writer?.writer?.dispose()
        isRecording = false
    }

    private fun completeRecording(chunk: ChunkData) {
        pauseRecording()
        waveformDrawableProperty.set(null)
        volumebarDrawableProperty.set(null)
        loadChunkMedia(chunk.file, chunk).subscribe {
            it.imageLoading = false
        }
        recordingChunkProperty.set(null)
    }

    private fun initializeRecordingAudio(file: File) {
        val pcmAudio = AudioFile(file)
        writer = WavFileWriter(pcmAudio, recorder.getAudioStream()) { /* no op */ }

        renderer = ActiveRecordingRenderer(
            recorder.getAudioStream(),
            writer!!.isWriting,
            width = 300,
            secondsOnScreen = 10
        )
        val waveformLayer = WaveformLayer(renderer!!)
        val volumeBar = VolumeBar(recorder.getAudioStream())

        waveformDrawableProperty.set(waveformLayer)
        volumebarDrawableProperty.set(volumeBar)
    }
}