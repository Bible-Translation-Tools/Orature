package org.wycliffeassociates.otter.jvm.controls.model

import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import java.time.LocalDateTime
import java.util.*

class TranslationCardModel2(
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val mode: ProjectMode,
    val modifiedTs: LocalDateTime?,
    val books: ObservableList<WorkbookDescriptor>
) {
    override fun hashCode(): Int {
        return Objects.hash(sourceLanguage.slug, targetLanguage.slug, mode.name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TranslationCardModel2
        if (
            sourceLanguage != other.sourceLanguage ||
            targetLanguage != other.targetLanguage ||
            mode != other.mode
        ) {
            return false
        }

        return true
    }

    fun getKey() = ProjectGroupKey(sourceLanguage.slug, targetLanguage.slug, mode)
}
