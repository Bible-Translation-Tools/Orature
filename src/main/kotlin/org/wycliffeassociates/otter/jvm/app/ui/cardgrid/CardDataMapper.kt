package org.wycliffeassociates.otter.jvm.app.ui.cardgrid

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter

class CardDataMapper {

    companion object {
        fun mapContentListToCards(contentList: List<Content>): List<CardData> {
            val cardList: MutableList<CardData> = mutableListOf()
            contentList.forEach {
                val newCardData = CardData(
                        it.labelKey,
                        CardDataType.CONTENT.value,
                        it.start.toString(),
                        it.sort,
                        contentSource = it
                )
                cardList.add(newCardData)
            }
            return cardList
        }

        fun mapChapterListToCards(chapterList: List<Chapter>): List<CardData> {
            val cardList: MutableList<CardData> = mutableListOf()
            chapterList.forEach {
                val newCardData = CardData(
                    ContentLabel.CHAPTER.value,
                    CardDataType.COLLECTION.value,
                    it.title,
                    it.sort,
                    chapterSource = it
                )
                cardList.add(newCardData)
            }
            return cardList
        }

        fun mapChapterToCardData(chapter: Chapter):CardData {
            val cardata = CardData(
                ContentLabel.CHAPTER.value,
                CardDataType.COLLECTION.value,
                chapter.title,
                chapter.sort,
                chapterSource = chapter
            )
            return cardata
        }
    }
}