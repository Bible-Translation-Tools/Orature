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

import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata

interface ArtworkDataSource {
    /**
     *  Searches for the appropriate artwork for a given project slug.
     *  A preferred aspect ratio can be provided, which will be returned if it exists.
     *  If a matching image exists, but not in the requested aspect ratio, the original image
     *  will be returned (in whichever aspect ratio it exists in).
     *
     *  If no matching image exists, null will be returned.
     *
     *  Aspect ratios will search for a file with the suffix _{width}x{height} appended to the file
     *  name (preceding the file extension). For example: gen_16x9.png
     *
     *  @param metadata metadata of the book/resource
     *  @param projectSlug project identifier
     *  @param imageRatio (Optional) preferred aspect ratio, by default, no aspect ratio will be used
     *
     *  @returns a nullable Artwork (which contains a file and attribution info). The included file returned contains the
     *  requested image, and null if no match was found.
     */
    fun getArtwork(
        metadata: ResourceMetadata,
        projectSlug: String,
        imageRatio: ImageRatio = ImageRatio.DEFAULT
    ): Artwork?
}
