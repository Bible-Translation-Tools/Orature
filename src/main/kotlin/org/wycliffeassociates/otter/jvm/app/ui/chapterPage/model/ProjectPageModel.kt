package org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.view.ChapterContext
import tornadofx.getProperty
import tornadofx.property

class ProjectPageModel {
    // pull the project from the dao in real use
    private val project = Project(
            Book(
                    "Romans",
                    listOf(
                            Chapter(
                                    1,
                                    listOf(
                                            Verse(false, "This is a verse 1.", 1, 0),
                                            Verse(true, "This is a verse 2.", 2, 0)
                                    )
                            ),
                            Chapter(
                                    2,
                                    listOf(
                                            Verse(true, "This is a verse 1.", 1, 0),
                                            Verse(false, "This is a verse 2.", 2, 0)
                                    )
                            )
                    )
            ),
            Language(
                    0,
                    "en",
                    "English",
                    true,
                    "English"
            ),
            Language(
                    0,
                    "fr",
                    "François",
                    false,
                    "French"
            )
    )

    // setup model with fx properties
    var bookTitle: String by property(project.book.title)
    val bookTitleProperty = getProperty(ProjectPageModel::bookTitle)

    var chapters: ObservableList<Chapter> by property(
            FXCollections.observableList(project.book.chapters)
    )
    val chaptersProperty = getProperty(ProjectPageModel::chapters)

    var visibleVerses: ObservableList<Verse> by property(
            FXCollections.observableList(project.book.chapters.first().verses.toMutableList())
    )
    val visibleVersesProperty = getProperty(ProjectPageModel::visibleVerses)

    var context: ChapterContext by property(ChapterContext.RECORD)
    var contextProperty = getProperty(ProjectPageModel::context)

    fun setChapter(chapterNumber: Int) {
        chapters
                .singleOrNull { it.number == chapterNumber }
                ?.let {
                    visibleVerses.clear()
                    visibleVerses.addAll(it.verses)
                }
    }

}
