package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Resource
import java.util.*

fun questionsDedup(questions: List<Question>): List<Question> {
    val filteredQuestions = mutableListOf<Question>()
    questions.forEach {question ->
        val match = filteredQuestions.find { it == question }
        if (match != null) {
            match.end = question.end
        } else {
            filteredQuestions.add(question)
        }
    }
    return filteredQuestions
}

data class Question(
    val start: Int,
    var end: Int,
    val resource: Resource?
) {

    val question: String?
        get() = resource
            ?.title
            ?.textItem
            ?.text

    val answer: String?
        get() = resource
            ?.body
            ?.textItem
            ?.text

    var review: String? = null

    override fun equals(other: Any?): Boolean =
        (other is Question)
                && (question == other.question)
                && (answer == other.answer)

    override fun hashCode(): Int = Objects.hash(start, end, question, answer)

    companion object {
        fun getQuestionsFromChunk(chunk: Chunk): List<Question> {
            val resourceGroup = chunk.resources.find {
                it.metadata.identifier == "tq"
            }
            return resourceGroup
                ?.resources
                ?.map { resource ->
                    Question(chunk.start, chunk.end, resource)
                }
                ?.blockingIterable()
                ?.toList() ?: listOf()
        }
    }
}