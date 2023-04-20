package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
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
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.PluginActions
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.narration.InsertTakeAudio
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NarrationHistory
import org.wycliffeassociates.otter.common.domain.narration.SplitAudioOnCues
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import org.wycliffeassociates.otter.jvm.controls.waveform.VolumeBar
import org.wycliffeassociates.otter.jvm.controls.waveform.ContinuousWaveformLayer
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.*
import tornadofx.*
import java.io.File
import javax.inject.Inject

private const val WAV_COLOR = "#015AD990"
private const val BACKGROUND_COLOR = "#FFFFFF00"

private const val INVERTED_WAV_COLOR = "#F2F5F3FF"
private const val INVERTED_BACKGROUND_COLOR = "#015AD9FF"

private const val PIXELS_PER_SECOND = 100

class ChapterNarrationViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ChapterPageViewModel::class.java)

    @Inject
    lateinit var player: IAudioPlayer
    private val audioController = AudioPlayerController(Slider())

    @Inject
    lateinit var splitAudioOnCues: SplitAudioOnCues
    @Inject
    lateinit var insertTakeAudio: InsertTakeAudio
    @Inject
    lateinit var concatenateAudio: ConcatenateAudio

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory
    @Inject
    lateinit var directoryProvider: IDirectoryProvider
    @Inject
    lateinit var narrationHistory: NarrationHistory

    val workbookDataStore: WorkbookDataStore by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    private val allChunks: ObservableList<ChunkData> = FXCollections.observableArrayList()
    val allSortedChunks = SortedList(allChunks, compareBy { it.sort })
    private val recordedSortedChunks = SortedList(allChunks, compareByDescending<ChunkData>  { it.sort })
    val recordedChunks = FilteredList(recordedSortedChunks) { it.hasAudio() }

    private val chapterList: ObservableList<Chapter> = observableListOf()
    val hasNextChapter = SimpleBooleanProperty(false)
    val hasPreviousChapter = SimpleBooleanProperty(false)

    val currentVerseLabelProperty = SimpleStringProperty()
    val floatingCardVisibleProperty = SimpleBooleanProperty()
    val onCurrentVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val initialSelectedItemProperty = SimpleObjectProperty<ChunkData>()

    var onScrollToChunk: (ChunkData) -> Unit = {}
    var onPlaybackStarted: (ChunkData) -> Unit = {}

    var recordStartedProperty = SimpleBooleanProperty(false)
    var recordStarted by recordStartedProperty

    var recordPausedProperty = SimpleBooleanProperty(false)
    var recordPaused by recordPausedProperty

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
    private var recordedAudio: AudioFile? = null

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()
    val contextProperty = SimpleObjectProperty(PluginType.RECORDER)
    val sourceAudioAvailableProperty = workbookDataStore.sourceAudioAvailableProperty
    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer?>(null)

    private enum class StepDirection {
        FORWARD,
        BACKWARD
    }

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        recordButtonTextProperty.bind(recordButtonTextBinding())
        audioPluginViewModel.pluginNameProperty.bind(pluginNameBinding())

        workbookDataStore.activeChapterProperty.onChange {
            narrationHistory.clear()
        }

        narrationHistory.setWorkbookDataStore(workbookDataStore)
    }

    fun dock() {
        allChunksLoadedProperty.onChangeWithDisposer { loaded ->
            if (loaded == true) {
                when {
                    recordedChunks.isNotEmpty() -> {
                        // Getting first() element because recordedChunks list is reverse sorted
                        val lastRecorded = recordedChunks.first()
                        val next = allSortedChunks.singleOrNull { it.sort == lastRecorded.sort + 1 }
                        next?.let {
                            initialSelectedItemProperty.set(it)
                        }
                    }
                    allSortedChunks.isNotEmpty() -> initialSelectedItemProperty.set(allSortedChunks.first())
                }
            }
        }.let { listeners.add(it) }

        workbookDataStore.activeWorkbookProperty.onChangeAndDoNowWithDisposer { workbook ->
            workbook?.let {
                getChapterList(workbook.target.chapters)
            }
        }.let(listeners::add)

        workbookDataStore.activeChapterProperty.onChangeAndDoNowWithDisposer { chapter ->
            chapter?.let {
                loading = true
                val totalChunks = chapter.chunkCount.blockingGet()

                setHasNextAndPreviousChapter()

                allChunks.onChangedObservable().subscribe {
                    if (totalChunks == it.size) {
                        allChunksLoaded = true
                        loading = false
                    } else {
                        allChunksLoaded = false
                    }
                }.also(disposables::add)

                loadChapter(chapter)

                recordedChunks.setPredicate { it.hasAudio() }
                openRecorder()
                openSourcePlayer()
            }
        }.let(listeners::add)
    }

    fun undock() {
        clearChapterState()

        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }

    private fun loadChapter(chapter: Chapter) {
        splitChapter(chapter)
            .andThen(loadChunks(chapter))
            .subscribe()
            .also(disposables::add)
    }

    private fun clearChapterState() {
        initialSelectedItemProperty.set(null)
        allChunksLoaded = false

        closePlayers()
        saveAndQuit()

        allChunks.clear()
        disposables.clear()
    }

    private fun splitChapter(chapter: Chapter): Completable {
        return chapter.audio.lastTake()?.file?.let {
            splitAudioOnCues.execute(it)
                .flatMapCompletable { chunkFiles ->
                    insertTakeAudio.insertChunksAudio(
                        workbookDataStore.workbook,
                        chapter,
                        chunkFiles
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
                data.onOpenApp = ::onChunkOpenIn
                data.onRecordAgain = ::onRecordAgain
                data.onWaveformClicked = { chunk ->
                    onScrollToChunk(chunk)
                }
                data.onRecord = ::onRecord
                data.onNext = ::onNext

                data.recordButtonTextProperty.bind(recordButtonTextProperty)

                it.audio.lastTake()?.file?.let { file ->
                    val audio = AudioFile(file)
                    data.file = file
                    data.start = 0
                    data.end = audio.reader().totalFrames

                    loadChunkMedia(audio.reader(), data)
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

    fun onUndoAction() {
        stopPlayer()
        save().subscribe {
            narrationHistory.undo()
            allChunks.clear()
            loadChapter(workbookDataStore.chapter)
        }
    }

    fun onRedoAction() {
        stopPlayer()
        save().subscribe {
            narrationHistory.redo()
            allChunks.clear()
            loadChapter(workbookDataStore.chapter)
        }
    }

    fun onChapterOpenIn() {
        narrationHistory.snapshot()
        processWithPlugin(workbookDataStore.chapter, PluginType.EDITOR)
    }

    fun onEditVerseMarkers() {
        narrationHistory.snapshot()
        processWithPlugin(workbookDataStore.chapter, PluginType.MARKER)
    }

    fun onChapterReset() {
        narrationHistory.snapshot()

        recordedAudio = null
        stopRecording()

        workbookDataStore.chapter.audio.getAllTakes().forEach {
            it.deletedTimestamp.accept(DateHolder.now())
        }

        workbookDataStore.chapter.chunks.getValues(emptyArray())
            .forEach {
                it.audio.getAllTakes().forEach {
                    it.deletedTimestamp.accept(DateHolder.now())
                }
            }

        recordedChunks.forEach { it.file = null }
        recordedChunks.setPredicate { it.hasAudio() }
        initialSelectedItemProperty.set(allSortedChunks.first())
    }

    fun nextChapter() {
        closePlayers()
        stepToChapter(StepDirection.FORWARD)
    }

    fun previousChapter() {
        closePlayers()
        stepToChapter(StepDirection.BACKWARD)
    }

    private fun stepToChapter(direction: StepDirection) {
        val step = when (direction) {
            StepDirection.FORWARD -> 1
            StepDirection.BACKWARD -> -1
        }
        val nextIndex = chapterList.indexOf(workbookDataStore.chapter) + step
        chapterList.elementAtOrNull(nextIndex)?.let {
            clearChapterState()
            workbookDataStore.activeChapterProperty.set(it)
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
                audioController.seek(0)
                audioController.play()

                playingChunkProperty.set(chunk)
                chunk.isPlayingProperty.bind(audioController.isPlayingProperty)
                audioController.audioSlider?.let { slider ->
                    chunk.playbackPositionProperty.bind(
                        slider.valueProperty().objectBinding { it?.toInt() ?: 0 }
                    )
                }
                chunk.totalFrames = player.getDurationInFrames()
            }
        }
    }

    private fun onRecord(chunk: ChunkData) {
        stopPlayer()

        val thisChunkInProgress = recordStarted && recordingChunkProperty.value == chunk
        val isNextChunkRecording = recordingChunkProperty.value?.let {
            it.sort == (chunk.sort - 1)
        } ?: true

        when {
            thisChunkInProgress && !recordPaused -> pauseRecording(chunk)
            thisChunkInProgress && recordPaused -> resumeRecording(chunk)
            !isNextChunkRecording -> {
                recordingChunkProperty.value?.let {
                    pauseRecording(it)
                }
                saveAndRecord(chunk)
            }
            else -> {
                if (!recordStarted) {
                    narrationHistory.snapshot()
                }
                record(chunk)
            }
        }
    }

    private fun onRecordAgain(chunkData: ChunkData) {
        saveAndRecord(chunkData)
    }

    private fun onChunkOpenIn(chunkData: ChunkData) {
        val chunk = workbookDataStore.chapter.chunks.getValues(emptyArray())
            .singleOrNull { it.sort == chunkData.sort }
        chunk?.let {
            narrationHistory.snapshot()
            processWithPlugin(it, PluginType.EDITOR)
        }
    }

    private fun onNext(chunk: ChunkData) {
        if (recordStarted && !recordPaused) {
            onRecord(chunk)
        }
    }

    private fun record(chunk: ChunkData) {
        onScrollToChunk(chunk)

        renderer?.clearData()

        recordingChunkProperty.value?.let {
            updateChunkData(it)
        }

        if (!recordStarted || recordPaused) {
            if (!recordStarted) {
                initializeRecordAudio()
            }

            recorder?.start()
            writer?.start()
        }

        recordedAudio?.file?.let {
            chunk.file = it
        }

        chunk.isDraft = true
        chunk.isRecording = true
        chunk.waveformProperty.bind(waveformDrawableProperty)
        chunk.volumeBarProperty.bind(volumebarDrawableProperty)

        recordedChunks.setPredicate { it.sort <= chunk.sort && it.hasAudio() }
        recordingChunkProperty.set(chunk)
        recordStarted = true
        recordPaused = false
    }

    private fun save(): Completable {
        recordingChunkProperty.value?.let {
            updateChunkData(it)
        }
        stopRecording()
        return saveRecordedAudio(compile = false)
    }

    private fun saveAndRecord(chunk: ChunkData) {
        save().subscribe {
            narrationHistory.snapshot()
            onRecord(chunk)
        }
    }

    private fun pauseRecording(chunk: ChunkData) {
        writer?.pause()
        recorder?.pause()

        updateChunkData(chunk)

        recordPaused = true
        recordedChunks.setPredicate { it.hasAudio() }
        onScrollToChunk(chunk)
    }

    private fun resumeRecording(chunk: ChunkData) {
        recorder?.start()
        writer?.start()

        chunk.isRecording = true
        chunk.waveformProperty.bind(waveformDrawableProperty)
        chunk.volumeBarProperty.bind(volumebarDrawableProperty)

        recordPaused = false
        recordedChunks.setPredicate { it.sort <= chunk.sort && it.hasAudio() }
    }

    private fun stopRecording() {
        writer?.pause()
        recorder?.pause()
        renderer?.clearData()

        writer?.writer?.dispose()
        recordStarted = false
        recordPaused = true
        recordingChunkProperty.set(null)
    }

    private fun saveAndQuit() {
        if (recordStarted) {
            recordingChunkProperty.value?.let {
                if (!recordPaused) {
                    pauseRecording(it)
                }
            }
            stopRecording()
            closeRecorder()
        }

        val shouldCompile = allChunks.size == recordedChunks.size && recordedChunks.isNotEmpty()
        saveRecordedAudio(shouldCompile)
            .subscribe()
    }

    private fun openRecorder() {
        recorder = audioConnectionFactory.getRecorder()
    }

    private fun closeRecorder() {
        recorder?.stop()
        recorder = null

        writer?.writer?.dispose()
        writer = null

        renderer?.removeListeners()
        renderer = null
    }

    fun openSourcePlayer() {
        workbookDataStore.sourceAudioProperty.value?.let { source ->
            val audioPlayer = (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
            audioPlayer.loadSection(source.file, source.start, source.end)
            sourceAudioPlayerProperty.set(audioPlayer)
        }
    }

    private fun stopPlayer() {
        audioController.pause()
        //audioController.seek(0)
        playingChunkProperty.value?.let {
            it.isPlayingProperty.unbind()
            it.playbackPositionProperty.unbind()
        }
        playingChunkProperty.set(null)
    }

    private fun closePlayers() {
        player.stop()
        player.release()
        audioController.release()
        playingChunkProperty.set(null)
        sourceAudioPlayerProperty.value?.close()
        sourceAudioPlayerProperty.set(null)
    }

    private fun createWaveformImage(
        reader: AudioFileReader,
        color: String,
        background: String
    ): Single<Image> {
        val width = (reader.totalFrames / DEFAULT_SAMPLE_RATE) * PIXELS_PER_SECOND

        return ObservableWaveformBuilder()
            .build(
                reader = reader,
                width = width,
                height = 176,
                wavColor = Color.web(color),
                background = Color.web(background)
            )
    }

    private fun initializeRecordAudio() {
        val tempFile = directoryProvider.createTempFile("audio", ".${AudioFileFormat.PCM.extension}")
        recorder?.let { _recorder ->
            recordedAudio = AudioFile(tempFile)
            writer = WavFileWriter(recordedAudio!!, _recorder.getAudioStream()) { /* no op */ }

            renderer = ActiveRecordingRenderer(
                _recorder.getAudioStream(),
                writer!!.isWriting,
                width = PIXELS_PER_SECOND,
                secondsOnScreen = 1,
                continuous = true
            )
            val continuousWaveformLayer = ContinuousWaveformLayer(renderer!!)
            val volumeBar = VolumeBar(_recorder.getAudioStream())

            writer?.let {
                renderer?.setRecordingStatusObservable(it.isWriting)
            }

            waveformDrawableProperty.set(continuousWaveformLayer)
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
                chunkData.end = recordedAudio?.totalFrames ?: 0
            } ?: run {
                chunkData.start = 0
                chunkData.end = recordedAudio?.totalFrames ?: 0
            }
        }
    }

    private fun updateChunkData(chunk: ChunkData) {
        chunk.isRecording = false
        chunk.waveformProperty.unbind()
        chunk.volumeBarProperty.unbind()
        updateAudioLocations(chunk)

        recordedAudio?.reader(chunk.start, chunk.end)?.let { reader ->
            loadChunkMedia(reader, chunk)
                .subscribe {
                    it.imageLoading = false
                }
                .also(disposables::add)
        }
    }

    private fun saveRecordedAudio(compile: Boolean): Completable {
        val chapter = workbookDataStore.chapter

        chapter.chunks.getValues(emptyArray()).forEach { chunk ->
            val chunkData = allChunks.singleOrNull { it.sort == chunk.sort }
            chunkData?.let {
                if (it.file == null) {
                    chunk.audio.getAllTakes().forEach {
                        it.deletedTimestamp.accept(DateHolder.now())
                    }
                }
            }
        }

        return recordedAudio?.let {
            val cues = mapChunkDataToCues()
            splitAudioOnCues.execute(it.file, cues)
                .doOnError { e ->
                    logger.error("Error in splitting temp chapter file into chunks", e)
                }
                .flatMapCompletable { chunkFiles ->
                    updateRecordedChunks(chunkFiles)
                    insertTakeAudio.insertChunksAudio(
                        workbookDataStore.workbook,
                        chapter,
                        chunkFiles
                    )
                        .doOnError { e ->
                            logger.error("Error in importing recorded chunks", e)
                        }
                }
                .andThen(Completable.defer {
                    if (compile) {
                        compileChapter(chapter)
                    } else {
                        Completable.complete()
                    }
                })
        } ?: run {
            if (compile) {
                compileChapter(chapter)
            } else {
                Completable.complete()
            }
        }
    }

    private fun compileChapter(chapter: Chapter): Completable {
        val files = recordedChunks.sortedBy { it.sort }.mapNotNull { it.file }
        return if (files.isNotEmpty()) {
            concatenateAudio.execute(files)
                .doOnError { e ->
                    logger.error("Error in concatenating chunks into temp chapter file", e)
                }
                .flatMapCompletable { chapterFile ->
                    insertTakeAudio.insertChapterAudio(
                        workbookDataStore.workbook,
                        chapter,
                        chapterFile
                    )
                        .doOnError { e ->
                            logger.error("Error in importing temp chapter file", e)
                        }
                }
        } else Completable.complete()
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
                    recordStarted && !recordPaused -> messages["pauseRecording"]
                    recordedChunks.isNotEmpty() || recordPaused -> messages["resumeRecording"]
                    else -> messages["beginRecording"]
                }
            },
            recordStartedProperty,
            recordPausedProperty,
            recordedChunks
        )
    }

    private fun mapChunkDataToCues(): List<AudioCue> {
        return recordedChunks.filter { it.isDraft }.map {
            AudioCue(location = it.start, label = it.sort.toString())
        }.sortedBy { it.location }
    }

    private fun processWithPlugin(rec: Recordable, pluginType: PluginType) {
        contextProperty.set(pluginType)

        saveRecordedAudio(false)
            .andThen(audioPluginViewModel
                .getPlugin(pluginType))
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType, ${e.message}")
            }
            .flatMapSingle { plugin ->
                rec.audio.lastTake()?.let { take ->
                    workbookDataStore.activeTakeNumberProperty.set(take.number)
                    fire(PluginOpenedEvent(pluginType, plugin.isNativePlugin()))
                    when (pluginType) {
                        PluginType.EDITOR -> audioPluginViewModel.edit(rec.audio, take)
                        PluginType.MARKER -> audioPluginViewModel.mark(rec.audio, take)
                        else -> null
                    }
                } ?: Single.just(PluginActions.Result.NO_PLUGIN)
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in processing take with plugin type: $pluginType - $e")
            }
            .onErrorReturn { PluginActions.Result.NO_PLUGIN }
            .subscribe { result ->
                when (result) {
                    PluginActions.Result.NO_PLUGIN -> {
                        snackBarObservable.onNext(messages["noEditor"])
                        narrationHistory.clearLastSnapshot()
                    }
                    else -> {
                        if (rec is Chunk) {
                            workbookDataStore.chapter.audio.getAllTakes().forEach {
                                it.deletedTimestamp.accept(DateHolder.now())
                            }
                        } else {
                            workbookDataStore.chapter.chunks.getValues(emptyArray()).forEach {
                                it.audio.getAllTakes().forEach {
                                    it.deletedTimestamp.accept(DateHolder.now())
                                }
                            }
                        }
                    }
                }
                fire(PluginClosedEvent(pluginType))
            }
    }

    private fun pluginNameBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                when (contextProperty.value) {
                    PluginType.RECORDER -> {
                        audioPluginViewModel.selectedRecorderProperty.value?.name
                    }
                    PluginType.EDITOR -> {
                        audioPluginViewModel.selectedEditorProperty.value?.name
                    }
                    PluginType.MARKER -> {
                        audioPluginViewModel.selectedMarkerProperty.value?.name
                    }
                    null -> throw IllegalStateException("Action is not supported!")
                }
            },
            contextProperty,
            audioPluginViewModel.selectedRecorderProperty,
            audioPluginViewModel.selectedEditorProperty,
            audioPluginViewModel.selectedMarkerProperty
        )
    }

    fun dialogTitleBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                String.format(
                    messages["sourceDialogTitle"],
                    workbookDataStore.activeTakeNumberProperty.value,
                    audioPluginViewModel.pluginNameProperty.value
                )
            },
            audioPluginViewModel.pluginNameProperty,
            workbookDataStore.activeTakeNumberProperty
        )
    }

    fun dialogTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                String.format(
                    messages["sourceDialogMessage"],
                    workbookDataStore.activeTakeNumberProperty.value,
                    audioPluginViewModel.pluginNameProperty.value,
                    audioPluginViewModel.pluginNameProperty.value
                )
            },
            audioPluginViewModel.pluginNameProperty,
            workbookDataStore.activeTakeNumberProperty
        )
    }

    private fun getChapterList(chapters: Observable<Chapter>) {
        chapters
            .toList()
            .map { it.sortedBy { chapter -> chapter.sort } }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in getting the chapter list", e)
            }
            .subscribe { list ->
                chapterList.setAll(list)
            }
    }

    private fun setHasNextAndPreviousChapter() {
        workbookDataStore.activeChapterProperty.value?.let { chapter ->
            if (chapterList.isNotEmpty()) {
                hasNextChapter.set(chapter.sort < chapterList.last().sort)
                hasPreviousChapter.set(chapter.sort > chapterList.first().sort)
            } else {
                hasNextChapter.set(false)
                hasPreviousChapter.set(false)
                chapterList.sizeProperty.onChangeOnce {
                    setHasNextAndPreviousChapter()
                }
            }
        } ?: run {
            hasNextChapter.set(false)
            hasPreviousChapter.set(false)
        }
    }

    private fun AssociatedAudio.lastTake(): Take? {
        return getAllTakes().lastOrNull {
            it.deletedTimestamp.value?.value == null
        }
    }
}