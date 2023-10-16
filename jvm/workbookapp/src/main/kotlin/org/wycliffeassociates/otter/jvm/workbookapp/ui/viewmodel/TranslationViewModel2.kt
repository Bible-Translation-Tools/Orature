package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkingStep
import tornadofx.ViewModel

class TranslationViewModel2 : ViewModel() {

    val workbookDataStore: WorkbookDataStore by inject()
    val audioDataStore: AudioDataStore by inject()

    val selectedStepProperty = SimpleObjectProperty<ChunkingStep>(null)
    val reachableStepProperty = SimpleObjectProperty<ChunkingStep>(ChunkingStep.BLIND_DRAFT)
    val sourceTextProperty = SimpleStringProperty()
    val currentMarkerProperty = SimpleIntegerProperty(-1)

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
    }

    fun navigateStep(target: ChunkingStep) {
        selectedStepProperty.set(target)
    }
}