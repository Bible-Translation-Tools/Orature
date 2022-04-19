/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.BookElement
import org.wycliffeassociates.otter.common.data.workbook.Chapter

class ChapterCardModel(
    val sort: Int,
    val title: String,
    val item: String,
    val dataType: String,
    val bodyText: String,
    val source: BookElement?,
    val onClick: (chapter: BookElement) -> Unit
) {
    constructor(title: String, chapter: Chapter, onClick: (chapter: BookElement) -> Unit) : this(
        sort = chapter.sort,
        title = title,
        item = ContentLabel.CHAPTER.value,
        dataType = CardDataType.COLLECTION.value,
        bodyText = chapter.title,
        source = chapter,
        onClick = onClick
    )
}
