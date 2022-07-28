package org.wycliffeassociates.otter.jvm.workbookapp.oqua

data class ChapterResults (
    val source: String,
    val target: String,
    val book: String,
    val chapter: Int,
    val results: List<QuestionResults>
)