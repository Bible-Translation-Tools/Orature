package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.domain.content.ResourceRecordable

data class Chunk(
    override val sort: Int,
    override val audio: AssociatedAudio,
    override val resources: List<ResourceGroup>,

    override val textItem: TextItem,
    val start: Int,
    val end: Int,
    override val contentType: ContentType

) : BookElement, ResourceRecordable {
    override val title
        get() = start.toString()
}