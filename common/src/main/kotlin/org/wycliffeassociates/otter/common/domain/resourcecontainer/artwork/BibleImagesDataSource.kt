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
        filesCache[projectSlug]?.let { return it }

        val imagesContainer = directoryProvider.resourceContainerDirectory
            .resolve(
                imagesContainerName.format(
                    metadata.language.slug, metadata.identifier
                )
            )

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
            val imgPath = rc.manifest.projects.firstOrNull {
                it.identifier == projectSlug
            }?.path

            if (imgPath != null) {
                val pathWithRatio = getImagePathWithRatio(imgPath, imageRatio)
                val path = if (rc.accessor.fileExists(pathWithRatio)) {
                    pathWithRatio
                } else if (rc.accessor.fileExists(imgPath)) {
                    imgPath
                } else {
                    return null
                }

                val image = cacheDir.resolve(File(path).name)
                    .apply { createNewFile() }

                image.deleteOnExit()
                image.outputStream().use { fos ->
                    rc.accessor.getInputStream(imgPath).use {
                        it.transferTo(fos)
                    }
                }

                filesCache[projectSlug] = image
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
