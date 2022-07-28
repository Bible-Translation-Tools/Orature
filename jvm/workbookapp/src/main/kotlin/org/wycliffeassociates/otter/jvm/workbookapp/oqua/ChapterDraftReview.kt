package org.wycliffeassociates.otter.jvm.workbookapp.oqua

data class ChapterDraftReview (
    val source: String,
    val target: String,
    val book: String,
    val chapter: Int,
    val draftReviews: List<QuestionDraftReview>
)