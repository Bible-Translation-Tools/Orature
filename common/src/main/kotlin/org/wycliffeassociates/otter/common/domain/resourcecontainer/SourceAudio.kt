package org.wycliffeassociates.otter.common.domain.resourcecontainer

import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import java.io.File

class SourceAudio(
    private val rc: ResourceContainer
) {

    fun get(project: String, chapter: Int): File? {
        if (rc.media != null) {
            val mediaProject = rc.media!!.projects.find { it.identifier == project }
            var media = mediaProject?.media?.find { it.identifier == ".mp3" }
            if (media == null) {
                media = mediaProject?.media?.find { it.identifier == ".wav" }
            }
            if (media != null) {
                return get(media, chapter)
            }
        }
        return null
    }

    fun get(media: Media, chapter: Int): File? {
        return if (rc.media != null && rc.accessor.fileExists(media.chapterUrl)) {
            val path = media.chapterUrl.replace("{chapter}", chapter.toString())
            val reader = rc.accessor.getReader(path)
            val extension = File(path).extension
            val temp = File.createTempFile("source", extension)
            temp.deleteOnExit()
            reader.transferTo(temp.bufferedWriter())
            return temp
        } else {
            null
        }
    }
}