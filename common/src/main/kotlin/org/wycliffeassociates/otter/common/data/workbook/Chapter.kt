package org.wycliffeassociates.otter.common.data.workbook

import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
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
                // Clean multiple line breaks to leave only one
                val text = "${elm.textItem.text.replace("\n", "")}\n"
                acc + "${verseLabel(elm.start, elm.end)}. $text"
            }
            .blockingGet()
        return TextItem(text, format!!)
    }

    private fun verseLabel(start: Int, end: Int): String {
        return if (start != end) {
            "$start-$end"
        } else {
            "$start"
        }
    }
}
