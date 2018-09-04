package org.wycliffeassociates.otter.jvm.app.ui.chapterPage.model

import io.reactivex.subjects.PublishSubject

class ChapterPageModel {
    var activeChapter = PublishSubject.create<Int>()
    var selectedTab = PublishSubject.create<String>()
//    val book: Observable<Book>
//    val project:Project
//    val chapters : Observable<List<Chapters>>
//    val verses: Observable<List<Verses>>

//    Demo Data
    val bookTitle = "Romans"

    //    val selectedTake
    val verse1 = Verse(false, "This is a verse", 1)
    val verse2 = Verse(true, "This is a verse", 2)
    val verse3 = Verse(false, "This is a verse", 3)

    // demo objects
    val chapters: List<Chapter> = listOf(
            Chapter(listOf(Verse(false, "Hey", 1)), 1),
            Chapter(listOf(verse1, verse2, verse3), 2),
            Chapter(listOf(verse1, verse2), 3),
            Chapter(listOf(verse1, verse2, verse3), 4),
            Chapter(listOf(verse1), 5))
}

//demo chapter
data class Chapter(
        val verses: List<Verse>,
        val chapterNumber: Int
)

//demo verse
data class Verse(
        val selected_take: Boolean,
        val text: String,
        val verseNumber: Int
)
