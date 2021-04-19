package org.wycliffeassociates.otter.common.domain.content

import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.*

interface Recordable {
    val audio: AssociatedAudio
    val contentType: ContentType
    val sort: Int
    val label: String
    val textItem: TextItem
}

interface ResourceRecordable : Recordable
