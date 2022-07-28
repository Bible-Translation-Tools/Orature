package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import com.fasterxml.jackson.module.kotlin.*
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject
class ResultsRepository
@Inject
constructor (
    private val directoryProvider: IDirectoryProvider
) {

    fun readResultsFile(workbook: Workbook, chapter: Int): ChapterResults {
        val accessor = getAccessor(workbook)
        val mapper = jacksonObjectMapper()
        val dir: File = accessor.getResultsFile()
        val file = File("${dir.absoluteFile}/ch${chapter}.json")

        return mapper.readValue(file)
    }

    fun writeResultsFile(workbook: Workbook, chapter: Int, questions: List<Question>) {
        val accessor = getAccessor(workbook)
        val mapper = jacksonObjectMapper()
        val dir: File = accessor.getResultsFile()
        val file = File("${dir.absoluteFile}/ch${chapter}.json")

        val results = ChapterResults(
            workbook.source.language.slug,
            workbook.target.language.slug,
            workbook.target.slug,
            chapter,
            questions.map(QuestionResults::mapFromQuestion)
        )

        dir.mkdirs()
        file.printWriter().use { out ->
            out.println(mapper.writeValueAsString(results))
        }
    }

    private fun getAccessor(workbook: Workbook): ProjectFilesAccessor {
        return ProjectFilesAccessor(
            directoryProvider,
            workbook.target.resourceMetadata,
            workbook.source.resourceMetadata,
            workbook.target.toCollection()
        )
    }
}