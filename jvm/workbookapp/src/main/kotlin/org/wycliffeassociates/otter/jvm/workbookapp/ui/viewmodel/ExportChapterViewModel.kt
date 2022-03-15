package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import org.wycliffeassociates.otter.common.data.primitives.Contributor
import tornadofx.*
import java.io.File

class ExportChapterViewModel : ViewModel() {
    private val workbookPageViewModel: WorkbookPageViewModel by inject()
    private val chapterViewModel: ChapterPageViewModel by inject()

    val contributors = observableListOf<Contributor>()

    fun export(outputDir: File) {
        chapterViewModel.exportChapter(outputDir)
    }

    fun loadContributors() {
        contributors.setAll(workbookPageViewModel.contributors)
    }

    fun removeContributor(index: Int) {

    }
}