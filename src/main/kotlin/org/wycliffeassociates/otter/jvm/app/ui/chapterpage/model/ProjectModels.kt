package org.wycliffeassociates.otter.jvm.app.ui.chapterpage.model

import org.wycliffeassociates.otter.common.data.model.Language

// TODO: Replace with actual (or global mocked) data model classes

//demo chapter
data class Chapter(
        val number: Int,
        val verses: List<Verse>
)

//demo verse
data class Verse(
        val hasSelectedTake: Boolean,
        val text: String,
        val verseNumber: Int,
        val selectedTakeNum: Int
)

data class Book(
        val title: String,
        val chapters: List<Chapter>
)

data class Project(
        val book: Book,
        val sourceLanguage: Language,
        val targetLanguage: Language
)