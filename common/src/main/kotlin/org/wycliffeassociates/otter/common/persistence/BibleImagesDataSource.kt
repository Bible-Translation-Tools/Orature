package org.wycliffeassociates.otter.common.persistence

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File

class BibleImagesDataSource(
    private val directoryProvider: IDirectoryProvider,
    private val imagesContainer: File
) : ImagesDataSource() {

    private val imagesDir = File(
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

        ResourceContainer.load(imagesContainer).use { rc ->
            val imgPath = rc.manifest.projects.firstOrNull {
                it.identifier == projectSlug
            }?.path

            if (imgPath != null && rc.accessor.fileExists(imgPath)) {
                val img = getImageFromRC(imgPath, rc)

                cacheImage(
                    metadata.language.slug,
                    metadata.identifier,
                    projectSlug,
                    img
                )
                return img
            }
        }

        return nextDataSource?.getImage(metadata, projectSlug)
    }

    private fun getImageFromRC(imgPath: String, rc: ResourceContainer): File {
        val relativeImgPath = File(imgPath)
        val img = imagesDir.resolve(relativeImgPath.name)
            .apply { createNewFile() }

        img.deleteOnExit()
        img.outputStream().use { fos ->
            rc.accessor.getInputStream(imgPath).use {
                it.transferTo(fos)
            }
        }
        return img
    }

    private fun cacheKey(metadata: ResourceMetadata, project: String): String {
        return "${metadata.language.slug}-${metadata.identifier}-$project"
    }

    companion object {
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
