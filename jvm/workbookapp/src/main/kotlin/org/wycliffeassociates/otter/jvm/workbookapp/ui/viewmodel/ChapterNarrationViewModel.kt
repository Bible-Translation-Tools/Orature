package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
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
import org.wycliffeassociates.otter.common.audio.*
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
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
    lateinit var concatenateAudio: ConcatenateAudio

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory
    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    val workbookDataStore: WorkbookDataStore by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    private val allChunks: ObservableList<ChunkData> = FXCollections.observableArrayList()
    val allSortedChunks = SortedList(allChunks, compareBy { it.sort })
    private val recordedSortedChunks = SortedList(allChunks, compareByDescending<ChunkData>  { it.sort })
    val recordedChunks = FilteredList(recordedSortedChunks) { it.hasAudio() }

    val currentVerseLabelProperty = SimpleStringProperty()
    val floatingCardVisibleProperty = SimpleBooleanProperty()
    val onCurrentVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val initialSelectedItemProperty = SimpleObjectProperty<ChunkData>()

    var onScrollToChunk: (ChunkData) -> Unit = {}
    var onPlaybackStarted: (ChunkData) -> Unit = {}

    var isRecordingProperty = SimpleBooleanProperty(false)
    var isRecording by isRecordingProperty

    var isRecordingPausedProperty = SimpleBooleanProperty(false)
    var isRecordingPaused by isRecordingPausedProperty

    val waveformDrawableProperty = SimpleObjectProperty<Drawable>()
    val volumebarDrawableProperty = SimpleObjectProperty<Drawable>()

    val playingChunkProperty = SimpleObjectProperty<ChunkData>()
    val recordingChunkProperty = SimpleObjectProperty<ChunkData>()
    val recordButtonTextProperty = SimpleStringProperty()
    private var loading: Boolean by property(false)
    private val loadingProperty = getProperty(ChapterNarrationViewModel::loading)

    private var allChunksLoaded: Boolean by property(false)
    private val allChunksLoadedProperty = getProperty(ChapterNarrationViewModel::allChunksLoaded)

    private val disposables = CompositeDisposable()
    private val listeners = mutableListOf<ListenerDisposer>()

    private var recorder: IAudioRecorder? = null
    private var writer: WavFileWriter? = null
    private var renderer: ActiveRecordingRenderer? = null
    private var recordAudio: AudioFile? = null

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        recordButtonTextProperty.bind(recordButtonTextBinding())
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
            loading = true
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

        recordedChunks.setPredicate { it.hasAudio() }
        openRecorder()
    }

    fun undock() {
        workbookDataStore.selectedChapterPlayerProperty.set(null)
        initialSelectedItemProperty.set(null)
        allChunksLoaded = false

        closePlayer()
        saveAndQuit()

        allChunks.clear()
        disposables.clear()
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
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

    private fun loadChunks(chapter: Chapter): Observable<ChunkData> {
        return chapter.chunks
            .doOnError { e ->
                logger.error("Error in loading chapter chunks: $chapter", e)
            }
            .observeOnFx()
            .flatMap {
                val data = ChunkData(it)
                data.onPlay = ::onPlay
                data.onOpenApp = ::onOpenIn
                data.onRecordAgain = ::onRecordAgain
                data.onWaveformClicked = { chunk ->
                    onScrollToChunk(chunk)
                }
                data.onRecord = ::onRecord
                data.onNext = ::onNext

                data.recordButtonTextProperty.bind(recordButtonTextProperty)
                data.isRecordingProperty.bind(isRecordingProperty)
                data.isRecordingPausedProperty.bind(isRecordingPausedProperty)

                it.audio.selected.value?.value?.file?.let { file ->
                    val audio = AudioFile(file)
                    audio.reader().use { reader ->
                        data.file = file
                        data.start = 0
                        data.end = reader.totalFrames

                        loadChunkMedia(reader, data)
                    }
                } ?: Observable.just(data)
            }
            .observeOnFx()
            .map {
                it.imageLoading = false
                allChunks.add(it)
                it
            }
    }

    private fun loadChunkMedia(
        reader: AudioFileReader,
        chunkData: ChunkData
    ): Observable<ChunkData> {
        chunkData.imageLoading = true

        return createWaveformImage(reader, WAV_COLOR, BACKGROUND_COLOR)
            .flatMap {
                chunkData.image = it
                createWaveformImage(reader, INVERTED_WAV_COLOR, INVERTED_BACKGROUND_COLOR)
            }.flatMapObservable {
                chunkData.invertedImage = it
                Observable.just(chunkData)
            }
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
                //onPlaybackStarted(chunk)

                player.loadSection(it, chunk.start, chunk.end)
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

    private fun onRecord(chunk: ChunkData) {
        stopPlayer()

        val thisChunkInProgress = isRecording && recordingChunkProperty.value == chunk
        val isNextChunkRecording = recordingChunkProperty.value?.let {
            it.sort == (chunk.sort - 1)
        } ?: true

        when {
            thisChunkInProgress && !isRecordingPaused -> pauseRecording(chunk)
            thisChunkInProgress && isRecordingPaused -> resumeRecording(chunk)
            !isNextChunkRecording -> {
                recordingChunkProperty.value?.let {
                    pauseRecording(it)
                }
                saveAndRecord(chunk)
            }
            else -> record(chunk)
        }
    }

    private fun onRecordAgain(chunk: ChunkData) {
        saveAndRecord(chunk)
    }

    private fun onOpenIn(chunk: ChunkData) {
        println("Opening the verse ${chunk.title} in an external app...")
    }

    private fun onNext(chunk: ChunkData) {
        if (isRecording && !isRecordingPaused) {
            onRecord(chunk)
        }
    }

    private fun record(chunk: ChunkData) {
        onScrollToChunk(chunk)
        recordedChunks.setPredicate { it.sort < chunk.sort && it.hasAudio() }

        renderer?.clearData()

        recordingChunkProperty.value?.let {
            updateChunkData(it)
        }

        if (!isRecording || isRecordingPaused) {
            if (!isRecording) {
                initializeRecordAudio()
            }

            recorder?.start()
            writer?.start()
        }

        recordAudio?.file?.let {
            chunk.file = it
        }

        chunk.isDraft = true
        recordingChunkProperty.set(chunk)
        isRecording = true
        isRecordingPaused = false
    }

    private fun saveAndRecord(chunk: ChunkData) {
        recordingChunkProperty.value?.let {
            updateChunkData(it)
        }
        stopRecording()
        saveRecordings(compile = false)
            .subscribe {
                onRecord(chunk)
            }
    }

    private fun pauseRecording(chunk: ChunkData) {
        writer?.pause()
        recorder?.pause()

        updateChunkData(chunk)

        isRecordingPaused = true
        recordedChunks.setPredicate { it.hasAudio() }
        onScrollToChunk(chunk)
    }

    private fun resumeRecording(chunk: ChunkData) {
        recorder?.start()
        writer?.start()

        isRecordingPaused = false
        recordedChunks.setPredicate { it.sort < chunk.sort && it.hasAudio() }
    }

    private fun stopRecording() {
        writer?.pause()
        recorder?.pause()
        renderer?.clearData()

        writer?.writer?.dispose()
        isRecording = false
        isRecordingPaused = true
        recordingChunkProperty.set(null)
    }

    private fun saveAndQuit() {
        if (isRecording) {
            recordingChunkProperty.value?.let {
                if (!isRecordingPaused) {
                    pauseRecording(it)
                }
            }
            stopRecording()
            closeRecorder()

            val shouldCompile = allChunks.size == recordedChunks.size
            saveRecordings(shouldCompile)
                .subscribe()
        }
    }

    private fun openRecorder() {
        recorder = audioConnectionFactory.getRecorder()
    }

    private fun closeRecorder() {
        recorder?.stop()
        recorder = null
    }

    private fun stopPlayer() {
        audioController.pause()
        audioController.seek(0)
        playingChunkProperty.value?.let {
            it.isPlayingProperty.unbind()
            it.playbackPositionProperty.unbind()
        }
        playingChunkProperty.set(null)
    }

    private fun closePlayer() {
        player.stop()
        player.release()
        audioController.release()
        playingChunkProperty.set(null)
    }

    private fun createWaveformImage(
        reader: AudioFileReader,
        color: String,
        background: String
    ): Single<Image> {
        val width = (reader.totalFrames / DEFAULT_SAMPLE_RATE) * 100

        return ObservableWaveformBuilder()
            .build(
                reader = reader,
                width = width,
                height = 120,
                wavColor = Color.web(color),
                background = Color.web(background)
            )
    }

    private fun initializeRecordAudio() {
        val tempFile = directoryProvider.createTempFile("audio", ".${AudioFileFormat.PCM.extension}")
        recorder?.let { _recorder ->
            recordAudio = AudioFile(tempFile)
            writer = WavFileWriter(recordAudio!!, _recorder.getAudioStream()) { /* no op */ }

            renderer = ActiveRecordingRenderer(
                _recorder.getAudioStream(),
                writer!!.isWriting,
                width = 300,
                secondsOnScreen = 10
            )
            val waveformLayer = WaveformLayer(renderer!!)
            val volumeBar = VolumeBar(_recorder.getAudioStream())

            writer?.let {
                renderer?.setRecordingStatusObservable(it.isWriting)
            }

            waveformDrawableProperty.set(waveformLayer)
            volumebarDrawableProperty.set(volumeBar)
        }
    }

    private fun updateRecordedChunks(files: Map<String, File>) {
        files.forEach { (title, file) ->
            recordedChunks.singleOrNull { it.title == title }?.let {
                val audio = AudioFile(file)
                it.isDraft = false
                it.file = file
                it.start = 0
                it.end = audio.totalFrames
            }
        }
    }

    private fun updateAudioLocations(chunkData: ChunkData) {
        chunkData.file?.let {
            val prevChunk = getPrevChunkData(chunkData.sort)
            prevChunk?.let {
                chunkData.start = prevChunk.end
                chunkData.end = recordAudio?.totalFrames ?: 0
            } ?: run {
                chunkData.start = 0
                chunkData.end = recordAudio?.totalFrames ?: 0
            }
        }
    }

    private fun updateChunkData(chunk: ChunkData) {
        updateAudioLocations(chunk)

        recordAudio?.reader(chunk.start, chunk.end)?.use { reader ->
            loadChunkMedia(reader, chunk)
                .subscribe {
                    it.imageLoading = false
                }
                .also(disposables::add)
        }
    }

    private fun saveRecordings(compile: Boolean): Completable {
        val chapter = workbookDataStore.chapter
        val chunks = chapter.chunks.getValues(emptyArray())

        return recordAudio?.let {
            val cues = mapChunkDataToCues()
            splitAudioOnCues.execute(it.file, cues)
                .doOnError { e ->
                    logger.error("Error in splitting temp chapter file into chunks", e)
                }
                .flatMapCompletable { chunkFiles ->
                    updateRecordedChunks(chunkFiles)
                    importChunks.execute(
                        workbookDataStore.workbook,
                        chapter,
                        workbookDataStore.activeResourceMetadata,
                        workbookDataStore.activeProjectFilesAccessor,
                        chunkFiles,
                        chunks
                    )
                        .doOnError { e ->
                            logger.error("Error in importing recorded chunks", e)
                        }
                }
                .andThen(Completable.defer {
                    if (compile) {
                        val files = recordedChunks.sortedBy { it.sort }.mapNotNull { it.file }
                        concatenateAudio.execute(files)
                            .doOnError { e ->
                                logger.error("Error in concatenating chunks into temp chapter file", e)
                            }
                            .flatMapCompletable { chapterFile ->
                            audioPluginViewModel.import(chapter, chapterFile, takeNumber = 1)
                                .doOnError { e ->
                                    logger.error("Error in importing temp chapter file", e)
                                }
                        }
                    } else {
                        Completable.complete()
                    }
                })
        } ?: Completable.complete()
    }

    private fun getPrevChunkData(sort: Int): ChunkData? {
        return allChunks
            .filter { it.isDraft }
            .singleOrNull { it.sort == (sort - 1) }
    }

    private fun recordButtonTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                when {
                    isRecording && !isRecordingPaused -> messages["pauseRecording"]
                    recordedChunks.isNotEmpty() || isRecordingPaused -> messages["resumeRecording"]
                    else -> messages["beginRecording"]
                }
            },
            isRecordingProperty,
            isRecordingPausedProperty,
            recordedChunks
        )
    }

    private fun mapChunkDataToCues(): List<AudioCue> {
        return recordedChunks.filter { it.isDraft }.map {
            AudioCue(location = it.start, label = it.sort.toString())
        }.sortedBy { it.location }
    }
}