package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ProjectViewModel: ViewModel() {
    private val wbDataStore: WorkbookDataStore by inject()

    val chaptersProperty = Bindings.createObjectBinding(
        {
            wbDataStore.activeWorkbookProperty.value?.let { workbook ->
                getChapters(workbook).asObservable()
            }
        },
        wbDataStore.activeWorkbookProperty
    )

    private fun getChapters(workbook: Workbook): List<Chapter> {
        return workbook.target.chapters.toList().blockingGet().filter { chapter ->
            chapterHasAudio(chapter)
        }
    }

    private fun chapterHasAudio(chapter: Chapter) =
        chapter.audio.selected.value?.value != null

    fun dock() {}
    fun undock() {}
}