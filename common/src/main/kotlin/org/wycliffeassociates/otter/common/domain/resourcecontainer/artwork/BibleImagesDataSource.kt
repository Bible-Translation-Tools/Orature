package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File

class BibleImagesDataSource(
    private val directoryProvider: IDirectoryProvider
) : ImagesDataSource() {

    private val cacheDir = File(
        directoryProvider.cacheDirectory,
        "bible-images"
    ).apply { mkdirs() }

    override fun getImage(
        metadata: ResourceMetadata,
        projectSlug: String
    ): File? {

        getImageFromCache(
            metadata.language.slug,
            metadata.identifier,
            projectSlug
        )?.let { return it }

        val imagesContainer = directoryProvider.resourceContainerDirectory
            .resolve(
                imagesContainerName.format(metadata.language.slug, metadata.identifier)
            )

        return if (imagesContainer.exists()) {
            getImageFromRC(imagesContainer, metadata, projectSlug)
        } else {
            null
        }
    }

    private fun getImageFromRC(
        rcFile: File,
        metadata: ResourceMetadata,
        projectSlug: String
    ): File? {

        ResourceContainer.load(rcFile).use { rc ->
            val imgPath = rc.manifest.projects.firstOrNull {
                it.identifier == projectSlug
            }?.path

            if (imgPath != null && rc.accessor.fileExists(imgPath)) {
                val relativeImgPath = File(imgPath)
                val img = cacheDir.resolve(relativeImgPath.name)
                    .apply { createNewFile() }

                img.deleteOnExit()
                img.outputStream().use { fos ->
                    rc.accessor.getInputStream(imgPath).use {
                        it.transferTo(fos)
                    }
                }

                cacheImage(
                    metadata.language.slug,
                    metadata.identifier,
                    projectSlug,
                    img
                )
                return img
            }
        }

        return null
    }

    companion object {
        private const val imagesContainerName = "%s_%s_bible_artwork" // {languageSlug}_{resourceId}...
        private val filesCache = mutableMapOf<String, File>()

        private fun getImageFromCache(
            languageSlug: String,
            resourceId: String,
            project: String
        ): File? {
            synchronized(filesCache) {
                return filesCache["$languageSlug-$resourceId-$project"]
            }
        }

        private fun cacheImage(
            languageSlug: String,
            resourceId: String,
            project: String,
            file: File
        ) {
            synchronized(filesCache) {
                filesCache["$languageSlug-$resourceId-$project"] = file
            }
        }
    }
}
