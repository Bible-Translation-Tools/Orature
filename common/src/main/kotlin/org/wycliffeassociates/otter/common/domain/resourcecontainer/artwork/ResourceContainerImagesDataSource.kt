package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
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
                    media != null && media.url.isNotEmpty()
                ) {
                    val image = getImageFromRC(media, rc, projectSlug, imageRatio)
                    if (image != null) {
                        cacheImage(
                            image,
                            metadata.language.slug,
                            metadata.identifier,
                            projectSlug
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
            getImagePathWithRatio(media.url, imageRatio)
        )

        media.quality.forEach { quality ->
            val urlWithParameters = media.url
                .replace("{quality}", quality)
                .replace("{version}", media.version)
            paths.add(
                getImagePathWithRatio(urlWithParameters, imageRatio)
            )
        }

        val language = rc.manifest.dublinCore.language.identifier
        val resourceId = rc.manifest.dublinCore.identifier

        for (path in paths) {
            if (rc.accessor.fileExists(path)) {
                val fileName =
                    "${language}_${resourceId}_${project}_${File(path).name}"

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
