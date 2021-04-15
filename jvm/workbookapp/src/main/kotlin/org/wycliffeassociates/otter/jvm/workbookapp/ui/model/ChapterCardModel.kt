package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter

class ChapterCardModel(
    sort: Int,
    title: String,
    val item: String,
    val dataType: String,
    val bodyText: String,
    val source: Chapter?,
    val onClick: (chapter: Chapter) -> Unit
) : WorkbookItemModel(sort, title) {
    constructor(title: String, chapter: Chapter, onClick: (chapter: Chapter) -> Unit) : this(
        sort = chapter.sort,
        title = title,
        item = ContentLabel.CHAPTER.value,
        dataType = CardDataType.COLLECTION.value,
        bodyText = chapter.title,
        source = chapter,
        onClick = onClick
    )
}
