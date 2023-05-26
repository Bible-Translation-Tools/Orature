package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import java.time.LocalDateTime

data class WorkbookDescriptor(
    val id: Int,
    val slug: String,
    val title: String,
    val label: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val mode: ProjectMode,
    val progress: Double = 0.0,
    val lastModified: LocalDateTime? = null,
    val hasSourceAudio: Boolean = false
)