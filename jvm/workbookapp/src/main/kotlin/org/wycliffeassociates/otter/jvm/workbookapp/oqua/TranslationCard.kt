package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import tornadofx.*
import java.util.*

class TranslationCard (
    val translation: Translation,
    val projects: ObservableList<Workbook>,
    val hasQuestions: Boolean
) {

    fun sortProjects() {
        projects.sortBy { workbook -> workbook.source.sort }
    }

    override fun equals(other: Any?): Boolean =
        ((other is TranslationCard) && (translation == other.translation))

    override fun hashCode(): Int = Objects.hash(translation, projects)

    companion object {
        fun mapFromWorkbook (workbook: Workbook): TranslationCard {
            val translation = Translation(workbook.source.language, workbook.target.language, null)
            val projects = observableListOf(workbook)
            val hasQuestions = workbook.source.subtreeResources.any { resource -> resource.identifier == "tq" }

            return TranslationCard(translation, projects, hasQuestions)
        }
    }
}