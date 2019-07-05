package org.wycliffeassociates.otter.jvm.app.ui.cardgrid

import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk

class CardDataMapper {

    companion object {
        fun mapChunkToCardData(chunk: Chunk) = CardData(
            ContentLabel.VERSE.value,
            CardDataType.CONTENT.value,
            chunk.start.toString(),
            chunk.sort,
            chunkSource = chunk
        )

        fun mapChapterToCardData(chapter: Chapter) = CardData(
            ContentLabel.CHAPTER.value,
            CardDataType.COLLECTION.value,
            chapter.title,
            chapter.sort,
            chapterSource = chapter
        )
    }
}