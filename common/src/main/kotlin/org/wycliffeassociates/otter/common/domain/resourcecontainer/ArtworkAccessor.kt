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
package org.wycliffeassociates.otter.common.domain.resourcecontainer

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.BibleImagesDataSource
import org.wycliffeassociates.otter.common.persistence.CustomImagesDataSource
import org.wycliffeassociates.otter.common.persistence.DefaultImagesDataSource
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.ImagesDataSource
import java.io.File

class ArtworkAccessor(val directoryProvider: IDirectoryProvider) {

    fun getArtwork(metadata: ResourceMetadata, projectSlug: String): File? {
        val imgDataSource1: ImagesDataSource = CustomImagesDataSource()
        val imgDataSource2: ImagesDataSource = BibleImagesDataSource(directoryProvider, File("""\bible\images\container"""))
        val imgDataSource3: ImagesDataSource = DefaultImagesDataSource()

        imgDataSource1.setFallbackDataSource(imgDataSource2)
        imgDataSource2.setFallbackDataSource(imgDataSource3)

        return imgDataSource1.getImage(metadata, projectSlug)
    }
}
