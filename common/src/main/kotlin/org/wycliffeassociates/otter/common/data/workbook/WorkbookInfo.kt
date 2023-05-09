package org.wycliffeassociates.otter.common.data.workbook

import java.time.LocalDateTime

data class WorkbookInfo(
    val collectionId: Int,
    val slug: String,
    val title: String,
    val label: String,
    val progress: Double,
    val lastModified: LocalDateTime,
    val hasSourceAudio: Boolean = false
)