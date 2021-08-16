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
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class BibleImagesDataSource(
    private val directoryProvider: IDirectoryProvider
) : ImagesDataSource {

    private val cacheDir = File(
        directoryProvider.cacheDirectory,
        "bible-images"
    ).apply { mkdirs() }

    override fun getImage(
        metadata: ResourceMetadata,
        projectSlug: String,
        imageRatio: ImageRatio
    ): File? {
        // fetch and return from cache if any
        filesCache[projectSlug + imageRatio.getImageSuffix()]
            ?.let { return it }

        val imagesContainer = directoryProvider
                                            .resourceContainerDirectory
                                            .resolve(imagesContainerName)

        return if (imagesContainer.exists()) {
            getImageFromRC(imagesContainer, projectSlug, imageRatio)
        } else {
            null
        }
    }

    private fun getImageFromRC(
        rcFile: File,
        projectSlug: String,
        imageRatio: ImageRatio
    ): File? {

        ResourceContainer.load(rcFile).use { rc ->
            val contentPath = rc.manifest.projects.firstOrNull {
                it.identifier == projectSlug
            }?.path

            if (contentPath != null) {
                val pathWithRatio = getImagePathWithRatio(contentPath, imageRatio)
                val imagePath = if (rc.accessor.fileExists(pathWithRatio)) {
                    pathWithRatio
                } else if (rc.accessor.fileExists(contentPath)) {
                    contentPath
                } else {
                    return null
                }

                val image = cacheDir.resolve(File(imagePath).name)
                    .apply { createNewFile() }

                image.deleteOnExit()
                image.outputStream().use { fos ->
                    rc.accessor.getInputStream(imagePath).use {
                        it.transferTo(fos)
                    }
                }

                filesCache[projectSlug + imageRatio.getImageSuffix()] = image
                return image
            }
        }

        return null
    }

    companion object {
        private const val imagesContainerName = "bible_artwork"
        private val filesCache = ConcurrentHashMap<String, File>()
    }
}
