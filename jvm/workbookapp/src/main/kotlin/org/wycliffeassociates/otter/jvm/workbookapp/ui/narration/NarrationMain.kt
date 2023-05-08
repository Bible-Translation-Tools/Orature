package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import javafx.stage.Stage
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NarrationApp() : App(NarrationView::class) {

    val workbookDataStore by inject<WorkbookDataStore>()

    init {
        mockWorkbook()

        tryImportStylesheet(resources["/css/theme/dark-theme.css"])
        tryImportStylesheet(resources["/css/theme/light-theme.css"])
        tryImportStylesheet(resources["/css/common.css"])
        tryImportStylesheet(resources["/css/control.css"])
        tryImportStylesheet(resources["/css/app-bar.css"])

    }


    override fun start(stage: Stage) {
        super.start(stage)
        stage.height = 600.0
        stage.width = 800.0
        stage.scene.root.addClass(org.wycliffeassociates.otter.common.data.ColorTheme.LIGHT.styleClass)
    }
    
    private fun mockWorkbook() {
        val workbook = mockk<Workbook>()
        val target = mockk<Book>()
        val source = mockk<Book>()

        mockBook(workbook, target)
        mockBook(workbook, source)

        every { workbook.target } returns target
        every { workbook.source } returns source

        workbookDataStore.activeWorkbookProperty.set(workbook)
        workbookDataStore.activeChapterProperty.set(target.chapters.blockingFirst())
    }

    private fun mockBook(workbook: Workbook, book: Book) {
        every { book.slug } returns "mat"
        every { book.title } returns "Matthew"
        every { book.chapters } returns mockChapters(book)
        mockResourceMetadata(book)
    }

    private fun mockResourceMetadata(book: Book) {
        every { book.resourceMetadata } returns mockk {
            every { language } returns mockk {
                every { name } returns "English"
                every { license } returns "Public Domain"
            }
        }
    }

    private fun mockChapters(book: Book): Observable<Chapter> {
        val chapters = mutableListOf<Chapter>()
        for (i in 1..28) {
            val chapter = mockk<Chapter>()
            every { chapter.text } returns "Chapter Text $i"
            every { chapter.title } returns i.toString()
            // every { chapter.chunks } returns mockVerses(chapter)
            chapters.add(chapter)
        }
        return Observable.fromIterable(chapters)
    }
}

fun main() {
    launch<NarrationApp>()
}