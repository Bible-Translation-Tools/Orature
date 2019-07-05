package org.wycliffeassociates.otter.jvm.app.ui.cardgrid

import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.workbook.Chapter

data class CardData(
    val item: String,
    val dataType: String,
    val bodyText : String,
    val sort: Int,
    val contentSource: Content? = null,
    val chapterSource: Chapter? = null
)