package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.primitives.Anthology
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import org.wycliffeassociates.otter.common.data.primitives.bookAnthology
import java.time.LocalDateTime

data class WorkbookDescriptor(
    val id: Int,
    val sourceCollection: Collection,
    val targetCollection: Collection,
    val mode: ProjectMode,
    val progress: Double = 0.0,
    val hasSourceAudio: Boolean = false
) {
    val slug: String = targetCollection.slug
    val title: String = targetCollection.titleKey
    val label: String = targetCollection.labelKey
    val sort: Int = sourceCollection.sort
    val lastModified: LocalDateTime? = targetCollection.modifiedTs

    val sourceLanguage: Language
        get() = sourceCollection.resourceContainer?.language
            ?: throw NullPointerException("Source metadata must not be null")

    val targetLanguage: Language
        get() = targetCollection.resourceContainer?.language
            ?: throw NullPointerException("Target metadata must not be null")

    val anthology = bookAnthology.getOrDefault(slug, Anthology.OTHER)
}