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
package org.wycliffeassociates.otter.common.data.workbook

import java.util.*
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.domain.content.ResourceRecordable

class Chunk(
    override val sort: Int,
    override val label: String,
    override val audio: AssociatedAudio,
    override val resources: List<ResourceGroup>,

    override val textItem: TextItem,
    val start: Int,
    val end: Int,

    var draftNumber: Int,

    override val contentType: ContentType

) : BookElement, ResourceRecordable {
    override val title
        get() = start.toString()

    override fun hashCode(): Int {
        return Objects.hash(
            sort,
            title,
            label,
            textItem,
            start,
            end,
            contentType
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chunk

        if (sort != other.sort) return false
        if (contentType != other.contentType) return false
        if (label != other.label) return false
        if (contentType != other.contentType) return false
        if (textItem != other.textItem) return false
        if (start != other.start) return false
        if (end != other.end) return false
        if (draftNumber != other.draftNumber) return false

        return true
    }

    override fun toString(): String {
        return "Chunk(sort=$sort, label=$label, textItem=$textItem, start=$start, end=$end, draftNumber=$draftNumber)"
    }
}
