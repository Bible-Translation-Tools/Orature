package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.content.*
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.TakeContext
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordScriptureViewModel : ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    val recordableViewModel = RecordableViewModel()

    // This will be bidirectionally bound to workbookViewModel's activeChunkProperty
    private val activeChunkProperty = SimpleObjectProperty<Chunk?>()
    private val activeChunk: Chunk
        get() = activeChunkProperty.value ?: throw IllegalStateException("Chunk is null")

    private var context: TakeContext by property(TakeContext.RECORD)
    val contextProperty = getProperty(RecordScriptureViewModel::context)

    var title: String by property()
    val titleProperty = getProperty(RecordScriptureViewModel::title)

    // Whether the UI should show the plugin as active
    private var showPluginActive: Boolean by property(false)
    var showPluginActiveProperty = getProperty(RecordScriptureViewModel::showPluginActive)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

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

    fun recordNewTake() {
        recordableViewModel.recordable?.let {
            contextProperty.set(TakeContext.RECORD)
            showPluginActive = true

            audioPluginViewModel
                .record(it)
                .observeOnFx()
                .doOnSuccess { result ->
                    showPluginActive = false
                    when (result) {
                        RecordTake.Result.NO_RECORDER -> snackBarObservable.onNext(messages["noRecorder"])
                        else -> {}
                    }
                }
                .subscribe()
        } ?: throw IllegalStateException("Recordable is null")
    }

    fun editTake(take: Take) {
        contextProperty.set(TakeContext.EDIT_TAKES)
        showPluginActive = true
        audioPluginViewModel.edit(take)
            .observeOnFx()
            .subscribe { result ->
                showPluginActive = false
                when (result) {
                    EditTake.Result.NO_EDITOR -> snackBarObservable.onNext(messages["noEditor"])
                    else -> {}
                }
            }
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

    private enum class StepDirection {
        FORWARD,
        BACKWARD
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