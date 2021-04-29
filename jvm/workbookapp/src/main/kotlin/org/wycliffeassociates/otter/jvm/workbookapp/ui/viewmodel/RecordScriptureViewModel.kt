package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import java.text.MessageFormat

class RecordScriptureViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(RecordScriptureViewModel::class.java)

    private enum class StepDirection {
        FORWARD,
        BACKWARD
    }

    private val workbookDataStore: WorkbookDataStore by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    val recordableViewModel = RecordableViewModel(audioPluginViewModel)

    // This will be bidirectionally bound to workbookViewModel's activeChunkProperty
    private val activeChunkProperty = SimpleObjectProperty<Chunk>()
    private val activeChunk: Chunk
        get() = activeChunkProperty.value ?: throw IllegalStateException("Chunk is null")

    private val titleProperty = SimpleStringProperty()
    private var title by titleProperty

    private val chunkList: ObservableList<Chunk> = observableListOf()
    val hasNext = SimpleBooleanProperty(false)
    val hasPrevious = SimpleBooleanProperty(false)

    private var activeChunkSubscription: Disposable? = null

    val breadcrumbTitleBinding = recordableViewModel.currentTakeNumberProperty.stringBinding {
        it?.let { take ->
            MessageFormat.format(
                messages["takeTitle"],
                messages["take"],
                take
            )
        } ?: messages["take"]
    }

    init {
        activeChunkProperty.bindBidirectional(workbookDataStore.activeChunkProperty)

        workbookDataStore.activeChapterProperty.onChangeAndDoNow { chapter ->
            chapter?.let {
                getChunkList(chapter.chunks)
                if (activeChunkProperty.value == null) {
                    recordableViewModel.recordable = it
                    setHasNextAndPrevious()
                }
            }
        }

        activeChunkProperty.onChangeAndDoNow { chunk ->
            setHasNextAndPrevious()
            if (chunk != null) {
                setTitle(chunk)
                // This will trigger loading takes in the RecordableViewModel
                recordableViewModel.recordable = chunk
            } else {
                workbookDataStore.activeChapterProperty.value?.let {
                    recordableViewModel.recordable = it
                }
            }
        }
    }

    fun nextChunk() {
        recordableViewModel.closePlayers()
        stepToChunk(StepDirection.FORWARD)
    }

    fun previousChunk() {
        recordableViewModel.closePlayers()
        stepToChunk(StepDirection.BACKWARD)
    }

    private fun setHasNextAndPrevious() {
        activeChunkProperty.value?.let { chunk ->
            if (chunkList.isNotEmpty()) {
                hasNext.set(chunk.start < chunkList.last().start)
                hasPrevious.set(chunk.start > chunkList.first().start)
            } else {
                hasNext.set(false)
                hasPrevious.set(false)
                chunkList.sizeProperty.onChangeOnce {
                    setHasNextAndPrevious()
                }
            }
        } ?: run {
            hasNext.set(false)
            hasPrevious.set(false)
        }
    }

    private fun setTitle(chunk: Chunk) {
        val label = messages["verse"]
        val start = chunk.start
        title = "$label $start"
    }

    private fun getChunkList(chunks: Observable<Chunk>) {
        activeChunkSubscription?.dispose()
        activeChunkSubscription = chunks
            .toList()
            .map { it.sortedBy { chunk -> chunk.start } }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in getting the chunk list", e)
            }
            .subscribe { list ->
                chunkList.setAll(list)
            }
    }

    private fun stepToChunk(direction: StepDirection) {
        val amount = when (direction) {
            StepDirection.FORWARD -> 1
            StepDirection.BACKWARD -> -1
        }
        chunkList
            .find { it.start == activeChunk.start + amount }
            ?.let { newChunk -> activeChunkProperty.set(newChunk) }
    }
}
