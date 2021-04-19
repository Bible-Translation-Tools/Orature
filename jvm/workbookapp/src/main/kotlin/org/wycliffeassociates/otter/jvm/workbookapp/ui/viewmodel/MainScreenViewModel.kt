package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import tornadofx.*

class MainScreenViewModel : ViewModel() {
    private val workbookDataStore: WorkbookDataStore by inject()

    val selectedProjectName = SimpleStringProperty()
    val selectedProjectLanguage = SimpleStringProperty()

    val selectedChapterTitle = SimpleStringProperty()
    val selectedChapterBody = SimpleStringProperty()

    val selectedChunkTitle = SimpleStringProperty()
    val selectedChunkBody = SimpleStringProperty()

    init {
        workbookDataStore.activeWorkbookProperty.onChange { workbook ->
            workbook?.let { projectSelected(workbook) }
        }

        workbookDataStore.activeChapterProperty.onChange { chapter ->
            chapter?.let { chapterSelected(chapter) }
        }

        workbookDataStore.activeChunkProperty.onChange { chunk ->
            chunk?.let { chunkSelected(chunk) }
        }
    }

    private fun projectSelected(selectedWorkbook: Workbook) {
        setActiveProjectText(selectedWorkbook)
    }

    private fun chapterSelected(chapter: Chapter) {
        setActiveChapterText(chapter)
    }

    private fun chunkSelected(chunk: Chunk) {
        setActiveChunkText(chunk)
    }

    private fun setActiveChunkText(chunk: Chunk) {
        selectedChunkTitle.set(messages[ContentLabel.of(chunk.contentType).value].toUpperCase())
        selectedChunkBody.set(chunk.start.toString())
    }

    private fun setActiveChapterText(chapter: Chapter) {
        selectedChapterTitle.set(messages["chapter"].toUpperCase())
        selectedChapterBody.set(chapter.title)
    }

    private fun setActiveProjectText(activeWorkbook: Workbook) {
        selectedProjectName.set(activeWorkbook.target.title)
        selectedProjectLanguage.set(activeWorkbook.target.language.name)
    }
}
