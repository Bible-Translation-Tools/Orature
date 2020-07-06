package org.wycliffeassociates.otter.common.domain.resourcecontainer

import java.io.File
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media

class CoverArtAccessor(val metadata: ResourceMetadata, val project: String) {
    companion object {
        private val cache = mutableMapOf<String, File>()
    }

    private val rc: ResourceContainer by lazy { ResourceContainer.load(metadata.path) }

    fun getArtwork(): File? {
        synchronized(cache) {
            cache[cacheKey(metadata, project)]?.let {
                return it
            }
        }
        if (rc.media != null) {
            val mediaProject = rc.media!!.projects.find { it.identifier == project }
            var media = mediaProject?.media?.find { it.identifier == "jpg" || it.identifier == "jpeg" }
            if (media == null) {
                media = mediaProject?.media?.find { it.identifier == "png" }
            }
            if (media != null) {
                return getArtwork(media)
            }
        }
        return null
    }

    private fun getArtwork(media: Media): File? {
        val paths = arrayListOf<String>()
        paths.add(media.chapterUrl)
        media.quality.forEach { quality ->
            paths.add(
                media.chapterUrl
                    .replace("{quality}", quality)
                    .replace("{version}", media.version)
            )
        }
        for (path in paths) {
            if (rc.accessor.fileExists(path)) {
                val file = createTempFile("${project}_artwork", ".jpg")
                file.deleteOnExit()
                file.outputStream().use { fos ->
                    rc.accessor.getInputStream(path).use {
                        it.transferTo(fos)
                    }
                }
                synchronized(cache) {
                    cache[cacheKey(metadata, project)] = file
                }
                return file
            }
        }
        return null
    }

    private fun cacheKey(metadata: ResourceMetadata, project: String): String {
        return "${metadata.language.slug}-${metadata.identifier}-$project"
    }
}
