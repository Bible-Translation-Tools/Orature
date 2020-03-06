package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio

data class Workbook(
    val source: Book,
    val target: Book
) {
    val sourceAudioAccessor: SourceAudio by lazy { SourceAudio(source.resourceMetadata, source.slug) }
}
