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
                val imagePath = getImagePathWithRatio(contentPath, imageRatio)
                if (!rc.accessor.fileExists(imagePath)) {
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
