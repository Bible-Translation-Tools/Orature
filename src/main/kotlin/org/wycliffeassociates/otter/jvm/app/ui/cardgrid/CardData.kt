package org.wycliffeassociates.otter.jvm.app.ui.cardgrid

import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk

data class CardData(
    val item: String,
    val dataType: String,
    val bodyText : String,
    val sort: Int,
    val chunkSource: Chunk? = null,
    val chapterSource: Chapter? = null
)