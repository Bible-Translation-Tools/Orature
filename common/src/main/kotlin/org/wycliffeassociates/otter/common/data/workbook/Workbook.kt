package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor

data class Workbook(
    val source: Book,
    val target: Book
) {
    val sourceAudioAccessor: SourceAudioAccessor by lazy { SourceAudioAccessor(source.resourceMetadata, source.slug) }
}
