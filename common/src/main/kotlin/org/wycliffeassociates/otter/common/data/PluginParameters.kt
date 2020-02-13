package org.wycliffeassociates.otter.common.data

import java.io.File

data class PluginParameters(
    val languageName: String,
    val bookTitle: String,
    val chapterLabel: String,
    val chapterNumber: Int,
    val unitLabel: String? = null,
    val unitNumber: Int? = null,
    val sourceChapterAudio: File? = null,
    val sourceChunkStart: Int? = null,
    val sourceChunkEnd: Int? = null
)