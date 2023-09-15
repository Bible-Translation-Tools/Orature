package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.*

class TranslationViewModel2 : ViewModel() {

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()

    val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(null)
    val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.CHUNKING)
    val selectedChunkProperty = SimpleIntegerProperty()
    val sourceTextProperty = SimpleStringProperty()
    val currentMarkerProperty = SimpleIntegerProperty(-1)
    val chunkList = observableListOf<ChunkViewData>()
    val chunkListProperty = SimpleListProperty<ChunkViewData>(chunkList)

    private val compositeDisposable = CompositeDisposable()

    fun dockPage() {
        val recentChapter = workbookDataStore.workbookRecentChapterMap.getOrDefault(
            workbookDataStore.workbook.hashCode(),
            1
        )
        val chapter = workbookDataStore.workbook.target.chapters
            .filter { it.sort == recentChapter }
            .blockingFirst()

        workbookDataStore.activeChapterProperty.set(chapter)
        workbookDataStore.getSourceText()
            .observeOnFx()
            .subscribe {
                sourceTextProperty.set(it)
            }

        updateStep()
    }

    fun undockPage() {
        selectedStepProperty.set(null)
        workbookDataStore.activeChapterProperty.set(null)
        workbookDataStore.activeWorkbookProperty.set(null)
        compositeDisposable.clear()
    }

    fun navigateStep(target: ChunkingStep) {
        selectedStepProperty.set(target)
    }

    fun updateStep() {
        workbookDataStore.chapter
            .chunks
            .observeOnFx()
            .subscribe { list ->
                if (list.isNotEmpty() && list.all { it.draftNumber > 0 }) {
                    reachableStepProperty.set(ChunkingStep.BLIND_DRAFT)
                    selectedStepProperty.set(ChunkingStep.BLIND_DRAFT)
                } else {
                    selectedStepProperty.set(ChunkingStep.CONSUME_AND_VERBALIZE)
                    reachableStepProperty.set(ChunkingStep.CHUNKING)
                }
            }.addTo(compositeDisposable)
    }
}