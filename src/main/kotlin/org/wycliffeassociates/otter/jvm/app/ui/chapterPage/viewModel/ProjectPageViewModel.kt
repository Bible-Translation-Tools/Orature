package org.wycliffeassociates.otter.jvm.app.ui.chapterPage.viewModel

import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.ProjectPageModel
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.view.ChapterContext
import tornadofx.*

class ProjectPageViewModel: ViewModel() {
    private val model = ProjectPageModel()
    val bookTitleProperty = bind { model.bookTitleProperty }
    val chaptersProperty = bind { model.chaptersProperty }
    val visibleVersesProperty = bind { model.visibleVersesProperty }
    val contextProperty = bind { model.contextProperty }

    fun changeContext(context: ChapterContext) {
        model.context = context
    }

    fun selectChapter(chapterIndex: Int) {
        model.setChapter(chapterIndex)
    }
}