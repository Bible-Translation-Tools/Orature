/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.data.workbook

import org.wycliffeassociates.otter.common.domain.resourcecontainer.ArtworkAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import java.util.*
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider

class Workbook(
    directoryProvider: IDirectoryProvider,
    val source: Book,
    val target: Book
) {
    val sourceAudioAccessor: SourceAudioAccessor by lazy {
        SourceAudioAccessor(
            directoryProvider,
            source.resourceMetadata,
            source.slug
        )
    }
    val artworkAccessor: ArtworkAccessor by lazy { ArtworkAccessor(directoryProvider) }

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
