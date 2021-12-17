/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import tornadofx.observableListOf

data class CardData(
    val item: String,
    val dataType: String,
    val bodyText: String,
    val sort: Int,
    val chunkSource: Chunk? = null,
    val chapterSource: Chapter? = null
) {
    val takes = observableListOf<TakeModel>()

    lateinit var player: IAudioPlayer
    var onChunkOpen: (CardData) -> Unit = {}
    var onTakeSelected: (CardData, TakeModel) -> Unit = { _, _ -> }

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
