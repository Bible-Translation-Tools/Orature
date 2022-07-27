package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.fasterxml.jackson.module.kotlin.*
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import java.io.File

fun gradeFromQuestion(question: Question): Grade {
    return Grade(
        question.question,
        question.answer,
        question.start,
        question.end,
        question.result
    )
}

data class Grade (
    val question: String?,
    val answer: String?,
    val start: Int,
    val end: Int,
    val result: String?
)

data class GradeContent (
    val source: String,
    val target: String,
    val book: String,
    val chapter: Int,
    val grades: List<Grade>
)

class GradeRepository (
    directoryProvider: IDirectoryProvider,
    wbDataStore: WorkbookDataStore
) {
    private val accessor = ProjectFilesAccessor(
        directoryProvider,
        wbDataStore.workbook.target.resourceMetadata,
        wbDataStore.workbook.source.resourceMetadata,
        wbDataStore.workbook.target.toCollection()
    )

    fun isFinished(chapter: Int): Boolean {
        return readGradeFile(chapter)?.grades?.all { grade -> grade.result != null } == true
    }

    fun readGradeFile(chapter: Int): GradeContent? {
        val mapper = jacksonObjectMapper()
        val dir: File = accessor.getGradeFile()
        val file = File("${dir.absoluteFile}/ch${chapter}.json")

        return try {
            mapper.readValue<GradeContent>(file)
        } catch (e: Exception) {
            null
        }
    }

    fun writeGradeFile(workbook: Workbook, chapter: Int, questions: List<Question>) {
        val mapper = jacksonObjectMapper()
        val dir: File = accessor.getGradeFile()
        val file = File("${dir.absoluteFile}/ch${chapter}.json")

        val grades = GradeContent(
            workbook.source.language.slug,
            workbook.target.language.slug,
            workbook.target.slug,
            chapter,
            questions.map(::gradeFromQuestion)
        )

        dir.mkdir()
        file.printWriter().use { out ->
            out.println(mapper.writeValueAsString(grades))
        }
    }
}