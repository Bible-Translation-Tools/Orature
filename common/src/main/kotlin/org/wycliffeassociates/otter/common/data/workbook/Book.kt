package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.data.model.Collection

data class Book(
    val collectionId: Int,
    val sort: Int,
    val slug: String,
    val title: String,
    val label: String,
    val chapters: Observable<Chapter>,
    val resourceMetadata: ResourceMetadata,
    val linkedResources: List<ResourceMetadata>,

    override val subtreeResources: List<ResourceMetadata>

) : BookElementContainer {
    val language: Language
        get() = resourceMetadata.language

    override val children: Observable<BookElement> = chapters.cast()

    fun toCollection(): Collection = Collection(sort, slug, label, title, resourceMetadata, collectionId)
}
