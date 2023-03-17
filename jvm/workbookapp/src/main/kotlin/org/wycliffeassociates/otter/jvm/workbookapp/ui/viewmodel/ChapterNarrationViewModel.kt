package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.onChangedObservable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.jvm.controls.waveform.ObservableWaveformBuilder
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.*
import java.io.File

private const val WAV_COLOR = "#015AD990"
private const val BACKGROUND_COLOR = "#FFFFFF00"

private const val INVERTED_WAV_COLOR = "#F2F5F3FF"
private const val INVERTED_BACKGROUND_COLOR = "#015AD9EE"

class ChapterNarrationViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ChapterPageViewModel::class.java)

    val workbookDataStore: WorkbookDataStore by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    val allChunks: ObservableList<ChunkData> = FXCollections.observableArrayList()
    val recordedChunks = FilteredList(allChunks) { it.hasAudio() }

    val currentVerseLabelProperty = SimpleStringProperty()
    val floatingCardVisibleProperty = SimpleBooleanProperty()
    val onCurrentVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val initialSelectedItemProperty = SimpleObjectProperty<ChunkData>()

    var onWaveformClicked: (ChunkData) -> Unit = {}

    private var loading: Boolean by property(false)
    private val loadingProperty = getProperty(ChapterNarrationViewModel::loading)

    private var allChunksLoaded: Boolean by property(false)
    private val allChunksLoadedProperty = getProperty(ChapterNarrationViewModel::allChunksLoaded)

    private val disposables = CompositeDisposable()
    private val listeners = mutableListOf<ListenerDisposer>()

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
            loadChapterContents(chapter)
        }
    }

    fun undock() {
        workbookDataStore.selectedChapterPlayerProperty.set(null)
        initialSelectedItemProperty.set(null)
        allChunksLoaded = false

        closePlayers()
        allChunks.clear()
        disposables.clear()
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }

    fun onChunkOpenIn(chunk: ChunkData) {
        println("Opening verse ${chunk.title} in external app...")
    }

    fun onChunkRecord(chunk: ChunkData) {
        println("Recording verse ${chunk.title}")
    }

    fun onRecordChunkAgain(chunk: ChunkData) {
        println("Recording verse ${chunk.title} again")
    }

    private fun closePlayers() {
        allChunks.forEach {
            it.player?.apply {
                stop()
                release()
            }
        }
    }

    private fun loadChapterContents(chapter: Chapter) {
        loading = true
        chapter.chunks
            .flatMapSingle { chunk ->
                audioPluginViewModel.saveChunk(chunk, chapter.audio)
                    .map { Pair(it, chunk) }
            }
            .doOnError { e ->
                logger.error("Error in splitting chapter into chunks for chapter: $chapter", e)
            }
            .observeOnFx()
            .subscribe { (result, chunk) ->
                if (result == TakeActions.Result.SUCCESS) {
                    chunk.audio.selected.value?.value?.file?.let { file ->
                        val chunkData = ChunkData(chunk)
                        chunkData.player = getPlayer()
                        chunkData.file = file
                        chunkData.player?.load(file)
                        allChunks.add(chunkData)

                        chunkData.imageLoading = true

                        createWaveformImage(file)
                            .observeOnFx()
                            .subscribe { image ->
                                chunkData.image = image
                                chunkData.imageLoading = false
                            }

                        createWaveformImage(file, true)
                            .observeOnFx()
                            .subscribe { image ->
                                println(image)
                                chunkData.invertedImage = image
                                chunkData.imageLoading = false
                            }
                    }
                } else {
                    val chunkData = ChunkData(chunk)
                    allChunks.add(chunkData)
                }
            }

        val totalChunks = chapter.chunkCount.blockingGet()
        allChunks.onChangedObservable().subscribe {
            if (totalChunks == it.size) {
                allChunksLoaded = true
                loading = false
            }
        }.let(disposables::add)
    }

    private fun createWaveformImage(file: File, inverted: Boolean = false): Single<Image> {
        val color = if (inverted) INVERTED_WAV_COLOR else WAV_COLOR
        val backgroundColor = if (inverted) INVERTED_BACKGROUND_COLOR else BACKGROUND_COLOR

        val audio = AudioFile(file)
        val reader = audio.reader()
        val width = (audio.reader().totalFrames / DEFAULT_SAMPLE_RATE) * 100

        return ObservableWaveformBuilder()
            .build(
                reader = reader,
                width = width,
                height = 120,
                wavColor = Color.web(color),
                background = Color.web(backgroundColor)
            )
    }

    private fun getPlayer(): IAudioPlayer {
        return (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
    }
}