package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordScriptureViewModel : ViewModel() {
    private enum class StepDirection {
        FORWARD,
        BACKWARD
    }

    private val workbookViewModel: WorkbookViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    val recordableViewModel = RecordableViewModel(audioPluginViewModel)

    // This will be bidirectionally bound to workbookViewModel's activeChunkProperty
    private val activeChunkProperty = SimpleObjectProperty<Chunk?>()
    private val activeChunk: Chunk
        get() = activeChunkProperty.value ?: throw IllegalStateException("Chunk is null")

    val titleProperty = SimpleStringProperty()
    var title by titleProperty

    private val chunkList: ObservableList<Chunk> = observableList()
    val hasNext = SimpleBooleanProperty(false)
    val hasPrevious = SimpleBooleanProperty(false)

    init {
        activeChunkProperty.bindBidirectional(workbookViewModel.activeChunkProperty)

        workbookViewModel.activeChapterProperty.onChangeAndDoNow {
            it?.let { chapter -> getChunkList(chapter.chunks) }
        }

        activeChunkProperty.onChangeAndDoNow {
            it?.let { chunk ->
                setTitle(chunk)
                setHasNextAndPrevious()
                // This will trigger loading takes in the RecordableViewModel
                recordableViewModel.recordable = chunk
            }
        }
    }

    fun nextChunk() {
        stepToChunk(StepDirection.FORWARD)
    }

    fun previousChunk() {
        stepToChunk(StepDirection.BACKWARD)
    }

    private fun setHasNextAndPrevious() {
        if (chunkList.isNotEmpty()) {
            doSetHasNextAndPrevious()
        } else {
            chunkList.isNotEmpty().toProperty().onChangeOnce {
                doSetHasNextAndPrevious()
            }
        }
    }

    private fun doSetHasNextAndPrevious() {
        if (chunkList.isNotEmpty()) {
            hasNext.set(activeChunk.start < chunkList.last().start)
            hasPrevious.set(activeChunk.start > chunkList.first().start)
        } else throw IllegalStateException("Chunk list is empty")
    }

    private fun setTitle(chunk: Chunk) {
        val label = ContentLabel.VERSE.value
        val start = chunk.start
        title = "$label $start"
    }

    private fun getChunkList(chunks: Observable<Chunk>) {
        chunks.toList()
            .observeOnFx()
            .subscribe { list ->
                chunkList.setAll(list.sortedBy { chunk -> chunk.start })
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