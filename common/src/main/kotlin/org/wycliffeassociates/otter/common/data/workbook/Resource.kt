package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.domain.content.ResourceRecordable

data class Resource(
    val title: Component,
    val body: Component?
) {
    data class Component(
        override val sort: Int,
        override val textItem: TextItem,
        override val audio: AssociatedAudio,
        override val contentType: ContentType
    ) : ResourceRecordable
}
