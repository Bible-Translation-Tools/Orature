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
import org.wycliffeassociates.resourcecontainer.entity.Media
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ResourceContainerImagesDataSource(
    private val directoryProvider: IDirectoryProvider
) : ImagesDataSource {

    private val cacheDir = File(
        directoryProvider.cacheDirectory,
        "bible-images-custom"
    ).apply { mkdirs() }

    override fun getImage(
        metadata: ResourceMetadata,
        projectSlug: String,
        imageRatio: ImageRatio
    ): File? {
        val ratioString = imageRatio.getImageSuffix()

        getImageFromCache(
            metadata.language.slug,
            metadata.identifier,
            projectSlug,
            ratioString
        )?.let { return it }

        ResourceContainer.load(metadata.path).use { rc ->
            val project = rc.media?.projects?.find { it.identifier == projectSlug }
            val mediaList = project?.media
                ?: return null

            mediaTypes.forEach { type ->
                val media = mediaList.find { it.identifier == type }
                if (
                    media != null && media.url.isNotEmpty()
                ) {
                    val image = getImageFromRC(media, rc, projectSlug, imageRatio)
                    if (image != null) {
                        cacheImage(
                            image,
                            metadata.language.slug,
                            metadata.identifier,
                            projectSlug,
                            ratioString
                        )
                        return image
                    }
                }
            }
        }

        return null
    }

    private fun getImageFromRC(
        media: Media,
        rc: ResourceContainer,
        project: String,
        imageRatio: ImageRatio
    ): File? {
        val paths = mutableListOf<String>()
        paths.add(
            filePathWithSuffix(media.url, imageRatio.getImageSuffix())
        )

        media.quality.forEach { quality ->
            val urlWithParameters = media.url
                .replace("{quality}", quality)
                .replace("{version}", media.version)
            paths.add(
                filePathWithSuffix(urlWithParameters, imageRatio.getImageSuffix())
            )
            paths.add(urlWithParameters) // if image ratio is not found
        }
        paths.add(media.url)

        val language = rc.manifest.dublinCore.language.identifier
        val resourceId = rc.manifest.dublinCore.identifier

        for (path in paths) {
            if (rc.accessor.fileExists(path)) {
                val fileName = "${language}_${resourceId}_${project}_${File(path).name}"

                val image = cacheDir.resolve(fileName)
                    .apply { createNewFile() }

                image.deleteOnExit()
                // copy image to cache dir
                image.outputStream().use { fos ->
                    rc.accessor.getInputStream(path).use {
                        it.transferTo(fos)
                    }
                }

                return image
            }
        }

        return null
    }

    companion object {
        private val mediaTypes = listOf("jpg", "jpeg", "png")
        // {languageSlug}-{resourceId}-{projectSlug}{ratio}
        private const val cacheKeyTemplate = "%s-%s-%s%s"
        private val filesCache = ConcurrentHashMap<String, File>()

        private fun getImageFromCache(
            languageSlug: String,
            resourceId: String,
            project: String,
            ratio: String
        ): File? {
            val key = cacheKeyTemplate.format(
                languageSlug, resourceId, project, ratio
            )
            return filesCache[key]
        }

        private fun cacheImage(
            image: File,
            languageSlug: String,
            resourceId: String,
            project: String,
            ratio: String
        ) {
            val key = cacheKeyTemplate.format(
                languageSlug, resourceId, project, ratio
            )
            filesCache[key] = image
        }
    }
}
