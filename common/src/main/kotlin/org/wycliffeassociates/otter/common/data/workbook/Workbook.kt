package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import java.util.*

class Workbook(
    val source: Book,
    val target: Book
) {
    val sourceAudioAccessor: SourceAudioAccessor by lazy { SourceAudioAccessor(source.resourceMetadata, source.slug) }

    override fun hashCode(): Int {
        return Objects.hash(
            source.collectionId,
            source.slug,
            source.language,
            target.collectionId,
            target.slug,
            target.language
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Workbook

        if (source != other.source) return false
        if (target != other.target) return false

        return true
    }
}
