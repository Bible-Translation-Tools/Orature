package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.*

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

        chunks.clear()
        recordedChunks.clear()
        //disposables.clear()
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
            var chunk: Chunk? = null
            chapter.chunks
                .flatMapSingle {
                    chunk = it
                    audioPluginViewModel.saveChunk(it, chapter.audio)
                }
                .doOnError { e ->
                    logger.error("Error in splitting chapter into chunks for chapter: $chapter", e)
                }
                .subscribe {
                    if (it == TakeActions.Result.SUCCESS && chunk != null) {
                        val player = getPlayer()
                        player.load(chunk!!.audio.selected.value!!.value!!.file)
                        val chunkData = ChunkData(chunk!!)
                        chunkData.player = player
                        recordedChunks.add(chunkData)
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

    private fun getPlayer(): IAudioPlayer {
        return (app as IDependencyGraphProvider).dependencyGraph.injectPlayer()
    }
}