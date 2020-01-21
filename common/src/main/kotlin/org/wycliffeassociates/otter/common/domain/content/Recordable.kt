package org.wycliffeassociates.otter.common.domain.content

import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.data.workbook.*

interface Recordable {
    val audio: AssociatedAudio
    val contentType: ContentType
    val sort: Int
}

interface ResourceRecordable : Recordable {
    val textItem: TextItem
}
