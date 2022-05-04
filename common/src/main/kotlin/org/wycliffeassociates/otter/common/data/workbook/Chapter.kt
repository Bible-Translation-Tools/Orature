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

import com.jakewharton.rxrelay2.Relay
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import java.lang.Thread.sleep
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.content.Recordable

data class Chapter(
    override val sort: Int,
    override val title: String,
    override val label: String,
    override val audio: AssociatedAudio,
    override val resources: List<ResourceGroup>,
    override val subtreeResources: List<ResourceMetadata>,
    val chunks: ReplayRelay<Chunk>,
    val addChunk: (List<Content>) -> Unit,
    val reset: () -> Unit
) : BookElement, BookElementContainer, Recordable {

    override val contentType: ContentType = ContentType.META
    override val children: Observable<BookElement> = chunks.cast()

    var text: String = ""

    override val textItem
        get() = textItem()

    private fun textItem(): TextItem {
        return TextItem(text, MimeType.USFM!!)
    }

    private fun verseLabel(start: Int, end: Int): String {
        return if (start != end) {
            "$start-$end"
        } else {
            "$start"
        }
    }
}
