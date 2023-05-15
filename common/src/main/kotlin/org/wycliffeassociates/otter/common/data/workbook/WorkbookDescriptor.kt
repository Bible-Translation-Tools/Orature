package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.primitives.Anthology
import java.time.LocalDateTime

data class WorkbookDescriptor(
    val collectionId: Int,
    val slug: String,
    val title: String,
    val label: String,
    val anthology: Anthology,
    val progress: Double = 0.0,
    val lastModified: LocalDateTime? = null,
    val hasSourceAudio: Boolean = false
)