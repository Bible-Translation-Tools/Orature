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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class ArtworkAccessor(
    val directoryProvider: IDirectoryProvider,
    val metadata: ResourceMetadata,
    val projectSlug: String
) {
    private val imagesDataSources = listOf<ImagesDataSource>(
        ResourceContainerImagesDataSource(),
        BibleImagesDataSource(directoryProvider),
        DefaultImagesDataSource()
    )

    fun getArtwork(): File? {
        imagesDataSources.forEach {
            val image = it.getImage(metadata, projectSlug)
            if (image != null) {
                return image
            }
        }

        return null
    }
}
