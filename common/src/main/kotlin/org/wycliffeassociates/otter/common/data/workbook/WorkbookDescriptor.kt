package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import java.time.LocalDateTime

data class WorkbookDescriptor(
    val id: Int,
    val slug: String,
    val title: String,
    val label: String,
    val sourceCollection: Collection,
    val targetCollection: Collection,
    val mode: ProjectMode,
    val progress: Double = 0.0,
    val lastModified: LocalDateTime? = null,
    val hasSourceAudio: Boolean = false
) {
    val sourceLanguage: Language
        get() = sourceCollection.resourceContainer?.language
            ?: throw NullPointerException("Source metadata must not be null")

    val targetLanguage: Language
        get() = targetCollection.resourceContainer?.language
            ?: throw NullPointerException("Target metadata must not be null")
}