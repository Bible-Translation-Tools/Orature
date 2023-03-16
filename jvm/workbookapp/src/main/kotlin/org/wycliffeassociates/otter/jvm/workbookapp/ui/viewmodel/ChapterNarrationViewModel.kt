package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.Single
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
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
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.*

private const val WAV_COLOR = "#015AD990"
private const val BACKGROUND_COLOR = "#FFFFFF00"

class ChapterNarrationViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ChapterPageViewModel::class.java)

    val workbookDataStore: WorkbookDataStore by inject()
    val audioPluginViewModel: AudioPluginViewModel by inject()

    val chunks: ObservableList<ChunkData> = FXCollections.observableArrayList()
    val recordedChunks: ObservableList<ChunkData> = FXCollections.observableArrayList()

    val currentVerseLabelProperty = SimpleStringProperty()
    val floatingCardVisibleProperty = SimpleBooleanProperty()
    val onCurrentVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val initialSelectedItemProperty = SimpleObjectProperty<ChunkData>()

    private val asyncBuilder = ObservableWaveformBuilder()
    val waveformMinimapImage = SimpleObjectProperty<Image>()

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(ChapterNarrationViewModel::loading)

    fun dock() {
        workbookDataStore.activeChapterProperty.value?.let { chapter ->
            splitChapter(chapter)
            loadChapterContents(chapter).subscribe()
        }
    }

    fun undock() {
        workbookDataStore.selectedChapterPlayerProperty.set(null)
        initialSelectedItemProperty.set(null)

        closePlayers()

        chunks.clear()
        recordedChunks.clear()
        //disposables.clear()
    }

    fun closePlayers() {
        recordedChunks.forEach {
            it.player?.apply {
                stop()
                release()
            }
        }
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

    private fun splitChapter(chapter: Chapter) {
        val hasAudio = chapter.audio.selected.value?.value != null
        if (hasAudio) {
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
                            chunkData.player?.load(file)
                            recordedChunks.add(chunkData)

                            val audioFile = AudioFile(file)
                            chunkData.imageLoadingProperty.set(true)
                            createWaveformImage(audioFile)
                                .observeOnFx()
                                .subscribe { image ->
                                    chunkData.imageProperty.set(image)
                                    chunkData.imageLoadingProperty.set(false)
                                }
                        }
                    }
                }
        }
    }

    private fun loadChapterContents(chapter: Chapter): Observable<ChunkData> {
        // Remove existing content so the user knows they are outdated
        loading = true
        return chapter.chunks
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in loading chapter contents for chapter: $chapter", e)
            }
            .map { ChunkData(it) }
            .map {
                chunks.add(it)
                chunks.sortBy { it.sort }

                if (it.sort == 1) {
                    initialSelectedItemProperty.set(it)
                }

                it
            }.observeOnFx()
    }

    private fun createWaveformImage(audio: AudioFile): Single<Image> {
        val reader = audio.reader()
        val width = (audio.reader().totalFrames / DEFAULT_SAMPLE_RATE) * 100
        return asyncBuilder
            .build(
                reader = reader,
                width = width,
                height = 100,
                wavColor = Color.web(WAV_COLOR),
                background = Color.web(BACKGROUND_COLOR)
            )
    }

    private fun getPlayer(): IAudioPlayer {
        return (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
    }
}