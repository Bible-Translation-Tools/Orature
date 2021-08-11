package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.entity.Media
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ResourceContainerImagesDataSource(
    private val directoryProvider: IDirectoryProvider
) : ImagesDataSource() {

    private val cacheDir = File(
        directoryProvider.cacheDirectory,
        "bible-images-custom"
    ).apply { mkdirs() }

    override fun getImage(metadata: ResourceMetadata, projectSlug: String): File? {
        getImageFromCache(
            metadata.language.slug,
            metadata.identifier,
            projectSlug
        )?.let { return it }

        ResourceContainer.load(metadata.path).use { rc ->
            val project = rc.media?.projects?.find { it.identifier == projectSlug }
            val mediaList = project?.media
                ?: return null

            mediaTypes.forEach { type ->
                val media = mediaList.find { it.identifier == type }
                if (
                    media != null && !media.url.isNullOrEmpty()
                ) {
                    val image = getImageFromRC(media, rc, metadata, projectSlug)
                    if (image != null) {
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
        metadata: ResourceMetadata,
        projectSlug: String
    ): File? {
        val paths = mutableListOf<String>()
        paths.add(media.url)
        media.quality.forEach { quality ->
            paths.add(
                media.url
                    .replace("{quality}", quality)
                    .replace("{version}", media.version)
            )
        }

        for (path in paths) {
            if (rc.accessor.fileExists(path)) {
                val image = cacheDir.resolve(File(path).name)
                    .apply { createNewFile() }

                image.deleteOnExit()
                // copy image to cache dir
                image.outputStream().use { fos ->
                    rc.accessor.getInputStream(path).use {
                        it.transferTo(fos)
                    }
                }

                cacheImage(
                    image,
                    metadata.language.slug,
                    metadata.identifier,
                    projectSlug
                )
                return image
            }
        }

        return null
    }

    companion object {
        private val mediaTypes = listOf("jpg", "jpeg", "png")
        private const val cacheKeyTemplate = "%s-%s-%s"
        private val filesCache = ConcurrentHashMap<String, File>()

        private fun getImageFromCache(
            languageSlug: String,
            resourceId: String,
            project: String
        ): File? {
            val key = cacheKeyTemplate.format(languageSlug, resourceId, project)
            return filesCache[key]
        }

        private fun cacheImage(
            image: File,
            languageSlug: String,
            resourceId: String,
            project: String
        ) {
            val key = cacheKeyTemplate.format(languageSlug, resourceId, project)
            filesCache[key] = image
        }
    }
}
