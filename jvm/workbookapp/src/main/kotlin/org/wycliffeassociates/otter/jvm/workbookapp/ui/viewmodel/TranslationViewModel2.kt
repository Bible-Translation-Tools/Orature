package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.controls.model.ChunkingStep
import tornadofx.*

class TranslationViewModel2 : ViewModel() {

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()

    val canUndoProperty = SimpleBooleanProperty(false)
    val canRedoProperty = SimpleBooleanProperty(false)
    val isFirstChapterProperty = SimpleBooleanProperty(false)
    val isLastChapterProperty = SimpleBooleanProperty(false)
    val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(null)
    val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.CHUNKING)
    val sourceTextProperty = SimpleStringProperty()
    val sourceInfoProperty = workbookDataStore.sourceInfoProperty
    val sourceLicenseProperty = workbookDataStore.sourceLicenseProperty
    val currentMarkerProperty = SimpleIntegerProperty(-1)
    val chunkList = observableListOf<ChunkViewData>()
    val chunkListProperty = SimpleListProperty<ChunkViewData>(chunkList)
    val selectedChunkBinding = workbookDataStore.activeChunkProperty.integerBinding { it?.sort ?: -1 }

    private val compositeDisposable = CompositeDisposable()

    fun dockPage() {
        val recentChapter = workbookDataStore.workbookRecentChapterMap.getOrDefault(
            workbookDataStore.workbook.hashCode(),
            1
        )
        navigateChapter(recentChapter)
    }

    fun undockPage() {
        selectedStepProperty.set(null)
        workbookDataStore.activeChapterProperty.set(null)
        workbookDataStore.activeWorkbookProperty.set(null)
        compositeDisposable.clear()
        resetUndoRedo()
    }

    fun nextChapter() {
        navigateChapter(workbookDataStore.chapter.sort + 1)
    }

    fun previousChapter() {
        navigateChapter(workbookDataStore.chapter.sort - 1)
    }

    fun navigateStep(target: ChunkingStep) {
        selectedStepProperty.set(target)
        resetUndoRedo()
    }

    fun selectChunk(chunkNumber: Int) {
        resetUndoRedo()
        workbookDataStore.chapter.chunks.value?.find { it.sort == chunkNumber }?.let {
            workbookDataStore.activeChunkProperty.set(it)
            audioDataStore.updateSourceAudio()
            audioDataStore.openSourceAudioPlayer()
            updateSourceText()
        }
    }

    fun loadChunks(chunks: List<Chunk>) {
        val chunkViewData = chunks.map { chunk ->
            val completed = when(selectedStepProperty.value) {
                ChunkingStep.BLIND_DRAFT -> chunk.hasSelectedAudio()
                ChunkingStep.PEER_EDIT -> chunk.checkingStatus().ordinal >= CheckingStatus.PEER_EDIT.ordinal
                ChunkingStep.KEYWORD_CHECK -> chunk.checkingStatus().ordinal >= CheckingStatus.KEYWORD.ordinal
                ChunkingStep.VERSE_CHECK -> chunk.checkingStatus().ordinal >= CheckingStatus.VERSE.ordinal
                else -> false
            }
            ChunkViewData(
                chunk.sort,
                completed,
                selectedChunkBinding
            )
        }
        chunkList.setAll(chunkViewData)

        updateStep()
    }

    fun updateStep(callback: () -> Unit = {}) {
        compositeDisposable.clear()

        workbookDataStore.chapter
            .chunks
            .observeOnFx()
            .subscribe { list ->
                when {
                    list.isEmpty() -> {
                        reachableStepProperty.set(ChunkingStep.CHUNKING)
                    }
                    list.all { it.checkingStatus() == CheckingStatus.VERSE } -> {
                        reachableStepProperty.set(ChunkingStep.CHAPTER_REVIEW)
                    }
                    list.all { it.checkingStatus().ordinal >= CheckingStatus.KEYWORD.ordinal } -> {
                        reachableStepProperty.set(ChunkingStep.VERSE_CHECK)
                    }
                    list.all { it.checkingStatus().ordinal >= CheckingStatus.PEER_EDIT.ordinal } -> {
                        reachableStepProperty.set(ChunkingStep.KEYWORD_CHECK)
                    }
                    list.all { it.hasSelectedAudio() } -> {
                        reachableStepProperty.set(ChunkingStep.PEER_EDIT)
                    }
                    list.isNotEmpty() -> {
                        reachableStepProperty.set(ChunkingStep.BLIND_DRAFT)
                    }
                }
                callback()
            }.addTo(compositeDisposable)
    }

    fun updateSourceText() {
        workbookDataStore.getSourceText()
            .observeOnFx()
            .doOnComplete {
                sourceTextProperty.set(null)
            }
            .subscribe {
                sourceTextProperty.set(it)
            }
    }

    private fun navigateChapter(chapter: Int) {
        selectedStepProperty.set(null)

        workbookDataStore.workbook.target
            .chapters
            .filter { it.sort == chapter }
            .singleElement()
            .observeOnFx()
            .subscribe {
                loadChapter(it)
            }
    }

    private fun loadChapter(chapter: Chapter) {
        workbookDataStore.activeChapterProperty.set(chapter)
        resetUndoRedo()
        updateSourceText()

        val wb = workbookDataStore.workbook
        wb.target
            .chapters
            .count()
            .observeOnFx()
            .subscribe { count ->
                isFirstChapterProperty.set(chapter.sort == 1)
                isLastChapterProperty.set(chapter.sort.toLong() == count)
            }

        val sourceAudio = wb.sourceAudioAccessor.getChapter(chapter.sort, wb.target)
        if (sourceAudio == null) {
            reachableStepProperty.set(null)
            compositeDisposable.clear()
        } else {
            updateStep {
                selectedStepProperty.set(reachableStepProperty.value)
            }
        }
    }

    private fun resetUndoRedo() {
        canUndoProperty.set(false)
        canRedoProperty.set(false)
    }
}