package org.wycliffeassociates.otter.jvm.app.ui.chapterPage.viewModel

import io.reactivex.rxkotlin.subscribeBy
import javafx.collections.FXCollections
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.ChapterPageModel
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model.Verse
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.*

class ChapterPageViewModel : ViewModel() {
    val model = ChapterPageModel()
//    val chapterPageUseCase = ChapterPageUseCase(Injector.chapterDao)
    private val modelChapters = model.chapters
    val activeChapter = model.activeChapter
    val selectedTab = model.selectedTab
    val chapters = FXCollections.observableArrayList<String>()!!
    val verses = FXCollections.observableArrayList<Verse>()!!
    val bookTitle = model.bookTitle

    fun changeContext(context: String) {
        selectedTab.onNext(context)
    }

    init {
        mapChapters()
    }

    private fun mapChapters() {
        var selectedChapter: Int
        modelChapters.map {
            // list the chapters before PublishSubject makes first push
            chapters.addAll(
                    messages["chapter"] + "\t" + it.chapterNumber.toString()
            )
        }

        //use observer to switch verses that are in verses observableList
        activeChapter.subscribeBy(
                onNext = {
                    selectedChapter = it
                    //clear the arrayList to prevent verse duplications
                    verses.clear()
                    modelChapters.map {
                        verses.addAll(
                                when (it.chapterNumber) {
                                    selectedChapter -> it.verses
                                    else -> listOf()
                                }
                        )
                    }
                }
        )
    }

    fun selectedChapter(chapterIndex: Int) {
        activeChapter.onNext(chapterIndex + 1)
    }
}