package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk

data class CardData(
    val item: String,
    val dataType: String,
    val bodyText: String,
    val sort: Int,
    val chunkSource: Chunk? = null,
    val chapterSource: Chapter? = null
) {
    constructor(chunk: Chunk) : this(
        item = ContentLabel.of(chunk.contentType).value,
        dataType = CardDataType.CONTENT.value,
        bodyText = chunk.start.toString(),
        sort = chunk.sort,
        chunkSource = chunk
    )

    constructor(chapter: Chapter) : this(
        item = ContentLabel.CHAPTER.value,
        dataType = CardDataType.COLLECTION.value,
        bodyText = chapter.title,
        sort = chapter.sort,
        chapterSource = chapter
    )
}
