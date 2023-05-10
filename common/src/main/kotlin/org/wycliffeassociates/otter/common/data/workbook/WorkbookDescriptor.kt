package org.wycliffeassociates.otter.common.data.workbook

import java.time.LocalDateTime

data class WorkbookDescriptor(
    val collectionId: Int,
    val slug: String,
    val title: String,
    val label: String,
    val progress: Double = 0.0,
    val lastModified: LocalDateTime? = null,
    val hasSourceAudio: Boolean = false
)