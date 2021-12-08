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
import org.wycliffeassociates.otter.common.utils.filePathWithSuffix
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class BibleArtworkDataSource(
    private val directoryProvider: IDirectoryProvider,
    private val imagesContainerNames: List<String> = listOf("en_art_sp.zip", "en_art_wa.zip")
) : ArtworkDataSource {

    private val cacheDir = File(
        directoryProvider.cacheDirectory,
        "bible-images"
    ).apply { mkdirs() }

    override fun getArtwork(
        metadata: ResourceMetadata,
        projectSlug: String,
        imageRatio: ImageRatio
    ): Artwork? {
        // fetch and return from cache if any
        artworkCache[projectSlug + imageRatio.getImageSuffix()]
            ?.let { return it }

        var art: Artwork? = null
        for (container in imagesContainerNames) {
            val imagesContainer = directoryProvider
                .resourceContainerDirectory
                .resolve(container)
            if (imagesContainer.exists()) {
                val found = getArtworkFromRC(imagesContainer, projectSlug, imageRatio)
                if (found != null) {
                    art = found
                    break
                }
            }
        }
        return art
    }

    private fun getArtworkFromRC(
        rcFile: File,
        projectSlug: String,
        imageRatio: ImageRatio
    ): Artwork? {

        ResourceContainer.load(rcFile).use { rc ->
            val contentPath = rc.manifest.projects.firstOrNull {
                it.identifier == projectSlug
            }?.path

            if (contentPath != null) {
                val pathWithRatio = filePathWithSuffix(contentPath, imageRatio.getImageSuffix())
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

                val artwork = Artwork(image, rc.manifest.dublinCore.creator, rc.manifest.dublinCore.rights)
                artworkCache[projectSlug + imageRatio.getImageSuffix()] = artwork
                return artwork
            }
        }

        return null
    }

    companion object {
        private val artworkCache = ConcurrentHashMap<String, Artwork>()
    }
}
