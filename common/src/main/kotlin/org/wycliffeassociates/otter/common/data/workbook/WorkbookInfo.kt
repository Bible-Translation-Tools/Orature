package org.wycliffeassociates.otter.common.data.workbook

import java.time.LocalDateTime

data class WorkbookInfo(
    val collectionId: Int,
    val title: String,
    val label: String,
    val progress: Double,
    val modifiedTimestamp: LocalDateTime,
    val hasSourceAudio: Boolean = false
)