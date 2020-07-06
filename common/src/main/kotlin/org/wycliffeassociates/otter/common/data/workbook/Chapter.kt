package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.data.model.MimeType
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.content.Recordable

data class Chapter(
    override val sort: Int,
    override val title: String,
    override val label: String,
    override val audio: AssociatedAudio,
    override val resources: List<ResourceGroup>,
    override val subtreeResources: List<ResourceMetadata>,
    val chunks: Observable<Chunk>
) : BookElement, BookElementContainer, Recordable {

    override val contentType: ContentType = ContentType.META
    override val children: Observable<BookElement> = chunks.cast()

    override val textItem
        get() = textItem()

    private fun textItem(): TextItem {
        var format: MimeType? = null
        val text = chunks
            .reduce("") { acc, elm ->
                if (format == null) format = elm.textItem.format
                acc + "${elm.textItem.text}\n"
            }
            .blockingGet()
        return TextItem(text, format!!)
    }
}
