package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata

data class Book(
    val sort: Int,
    val slug: String,
    val title: String,
    val chapters: Observable<Chapter>,
    val resourceMetadata: ResourceMetadata,

    override val subtreeResources: List<ResourceMetadata>

) : BookElementContainer {
    val language: Language
        get() = resourceMetadata.language

    override val children: Observable<BookElement> = chapters.cast()
}
