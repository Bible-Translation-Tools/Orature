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

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.cast
import io.reactivex.schedulers.Schedulers
import java.util.*
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.content.Recordable

class Chapter(
    override val sort: Int,
    override val title: String,
    override val label: String,
    override val audio: AssociatedAudio,
    override val resources: List<ResourceGroup>,
    override val subtreeResources: List<ResourceMetadata>,
    private val lazychunks: Lazy<BehaviorRelay<List<Chunk>>>,
    val chunkCount: Single<Int>,
    val addChunk: (List<Content>) -> Unit,
    val reset: () -> Unit
) : BookElement, BookElementContainer, Recordable {

    override val contentType: ContentType = ContentType.META
    override val children: Observable<BookElement> by lazy { getDraft().cast() }

    val chunks by lazychunks

    var text: String = ""

    override val textItem
        get() = textItem()

    fun hasSelectedAudio() = audio.selected.value?.value != null

    fun getSelectedTake() = audio.selected.value?.value

    fun getDraft(): Observable<Chunk> {
        return getLatestDraftFromRelay()
            .flattenAsObservable { it }
            .switchIfEmpty(Observable.empty<Chunk>())
            .subscribeOn(Schedulers.io())
    }

    private fun getLatestDraftFromRelay(): Maybe<List<Chunk>> {
        return Maybe.fromCallable { chunks.value }
    }

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

    override fun hashCode(): Int {
        return Objects.hash(
            sort,
            title,
            label,
            contentType
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chapter

        if (sort != other.sort) return false
        if (title != other.title) return false
        if (label != other.label) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun toString(): String {
        return "Chapter(sort=$sort, title=$title, label=$label, textItem=$textItem, contentType=$contentType)"
    }
}
