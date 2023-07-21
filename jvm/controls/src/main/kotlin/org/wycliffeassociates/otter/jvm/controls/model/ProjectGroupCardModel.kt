package org.wycliffeassociates.otter.jvm.controls.model

import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import java.time.LocalDateTime

class ProjectGroupCardModel(
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val mode: ProjectMode,
    val modifiedTs: LocalDateTime?,
    val books: ObservableList<WorkbookDescriptor>
) {
    fun getKey() = ProjectGroupKey(sourceLanguage.slug, targetLanguage.slug, mode)
}
