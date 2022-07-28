package org.wycliffeassociates.otter.jvm.workbookapp.oqua

data class QuestionResults (
    val question: String?,
    val answer: String?,
    val start: Int,
    val end: Int,
    val result: String?
) {
    companion object {
        fun mapFromQuestion(question: Question): QuestionResults {
            return QuestionResults(
                question.question,
                question.answer,
                question.start,
                question.end,
                question.result
            )
        }
    }
}