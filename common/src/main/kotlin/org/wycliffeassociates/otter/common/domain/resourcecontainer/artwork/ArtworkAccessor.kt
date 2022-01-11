/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider

class ArtworkAccessor(
    val directoryProvider: IDirectoryProvider,
    val metadata: ResourceMetadata,
    val projectSlug: String
) {
    private val artworkDataSources = listOf<ArtworkDataSource>(
        ResourceContainerArtworkDataSource(directoryProvider),
        BibleArtworkDataSource(directoryProvider)
    )

    /**
     *  Retrieves the most relevant artwork based on the given parameters.
     *  If imageRatio is specified but the result is not found,
     *  the original image will be returned (if exists). Otherwise,
     *  null is returned
     *
     *  @param imageRatio the aspect ratio preference of the requested image.
     *  A default ratio will be used if it the requested ratio is not found.
     *
     *  @return a nullable Artwork which contains the image or null if no match
     *  was found.
     */
    fun getArtwork(imageRatio: ImageRatio = ImageRatio.DEFAULT): Artwork? {
        artworkDataSources.forEach { dataSource ->
            var image = dataSource.getArtwork(metadata, projectSlug, imageRatio)
            if (image != null) {
                return image
            }
        }

        return null
    }
}
