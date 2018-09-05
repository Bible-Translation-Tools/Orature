package org.wycliffeassociates.otter.jvm.app.ui.chapterPage.viewModel

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import javafx.collections.FXCollections
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.ChapterPageModel
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.Verse
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.view.Contexts
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.*

class ChapterPageViewModel : ViewModel() {
    val model = ChapterPageModel()
    var activeChapter = PublishSubject.create<Int>()
    var selectedTab = PublishSubject.create<Contexts>()
    val chapters = FXCollections.observableArrayList<String>()!!
    val verses = FXCollections.observableArrayList<Verse>()!!
    val bookTitle = model.bookTitle

    fun changeContext(context: Contexts) {
        selectedTab.onNext(context)
    }

    init {
        initalizeChapterPage()
    }

    private fun initalizeChapterPage() {
        var selectedChapter: Int
        model.chapters.forEach {
            //map list of chapters to an observable Array List, prepend "Chapter" string
            chapters.addAll(
                    messages["chapter"] + " " + it.chapterNumber.toString()
            )
        }

        //use observer to switch verses that are in verses observableList
        activeChapter.subscribe {
                selectedChapter = it
                //clear the arrayList to prevent verse duplications
                verses.clear()
                verses.addAll(
                        model.chapters
                                .filter { it.chapterNumber == selectedChapter }
                                .first()
                                .verses
                )
            }
    }

    fun selectedChapter(chapterIndex: Int) {
        activeChapter.onNext(chapterIndex)
    }
}