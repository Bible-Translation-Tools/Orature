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

import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.primitives.Collection
import java.time.LocalDateTime

data class Book(
    val collectionId: Int,
    val sort: Int,
    val slug: String,
    val title: String,
    val label: String,
    val chapters: Observable<Chapter>,
    val resourceMetadata: ResourceMetadata,
    val linkedResources: List<ResourceMetadata>,
    val modifiedTs: LocalDateTime?,

    override val subtreeResources: List<ResourceMetadata>

) : BookElementContainer {
    val language: Language
        get() = resourceMetadata.language

    override val children: Observable<BookElement> = chapters.cast()

    fun toCollection(): Collection = Collection(
        sort,
        slug,
        label,
        title,
        resourceMetadata,
        modifiedTs,
        collectionId
    )
}
