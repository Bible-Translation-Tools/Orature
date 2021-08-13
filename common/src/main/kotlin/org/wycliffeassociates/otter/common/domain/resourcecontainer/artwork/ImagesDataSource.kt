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
import java.io.File

interface ImagesDataSource {
    fun getImage(
        metadata: ResourceMetadata,
        projectSlug: String,
        imageRatio: ImageRatio = ImageRatio.DEFAULT
    ): File?


    fun getImagePathWithRatio(
        path: String,
        ratio: ImageRatio
    ): String {
        if (ratio == ImageRatio.DEFAULT) {
            return path
        }
        val image = File(path)
        val nameWithRatio =
            "${image.nameWithoutExtension}_${ratio.getStringFormat()}.${image.extension}"

        return image.parentFile.resolve(nameWithRatio)
            .invariantSeparatorsPath
    }
}
