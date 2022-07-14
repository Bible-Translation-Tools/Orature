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

import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
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
    private val lazychunks: Lazy<ReplayRelay<Chunk>>,
    val chunkCount: Single<Int>,
    val addChunk: (List<Content>) -> Unit,
    val reset: () -> Unit
) : BookElement, BookElementContainer, Recordable {

    override val contentType: ContentType = ContentType.META
    override val children: Observable<BookElement> = getDraft().cast()

    val chunks by lazychunks

    var text: String = ""

    override val textItem
        get() = textItem()

    private fun getLatestDraftFromRelay(): Single<List<Chunk>> {
        return Single.fromCallable {
            val draft = mutableListOf<Chunk>()
            val chunkTotal = chunkCount.blockingGet()
            val disposable = chunks
                .filter { it.draftNumber > 0 } // filter active drafts
                .takeUntil { draft.size == chunkTotal }
                .forEach { draft.add(it) }
            while (draft.size != chunkTotal) {
                sleep(50)
            }
            disposable.dispose()
            draft.toList()
        }.subscribeOn(Schedulers.io())
    }

    fun getDraft(): Observable<Chunk> {
        return chunkCount
            .toObservable()
            .map {
                (getLatestDraftFromRelay().blockingGet()).toObservable()
            }
            .flatMap { it }
            .subscribeOn(Schedulers.io())
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
}
