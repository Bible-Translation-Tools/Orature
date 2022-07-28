package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ProjectViewModel: ViewModel() {
    private val wbDataStore: WorkbookDataStore by inject()

    val chapters = observableListOf<Chapter>()

    fun dock() {
        chapters.setAll(getChapters(wbDataStore.workbook).asObservable())
        wbDataStore.activeChapterProperty.set(null)
    }

    private fun getChapters(workbook: Workbook): List<Chapter> {
        return workbook
            .target
            .chapters
            .toList()
            .blockingGet()
            .filter { it.hasAudio() }
    }
}