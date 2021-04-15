package org.wycliffeassociates.otter.common.domain.resourcecontainer

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import java.io.File

data class SourceAudio(val file: File, val start: Int, val end: Int)

class SourceAudioAccessor(
    val metadata: ResourceMetadata,
    val project: String
) {

    val cache = mutableMapOf<String, File>()

    fun getChapter(chapter: Int): SourceAudio? {
        ResourceContainer.load(metadata.path).use { rc ->
            if (rc.media != null) {
                val mediaProject = rc.media!!.projects.find { it.identifier == project }
                var media = mediaProject?.media?.find { it.identifier == "mp3" }
                if (media == null) {
                    media = mediaProject?.media?.find { it.identifier == "wav" }
                }
                if (media != null) {
                    return getChapter(media, chapter, rc)
                }
            }
        }
        return null
    }

    private fun getChapter(media: Media, chapter: Int, rc: ResourceContainer): SourceAudio? {
        return if (rc.media != null && !media.chapterUrl.isNullOrEmpty()) {
            val path = media.chapterUrl.replace("{chapter}", chapter.toString())
            if (rc.accessor.fileExists(path)) {
                if (cache.containsKey(path) && cache[path] != null) {
                    val temp = cache[path]!!
                    val wav = WavFile(temp)
                    val size = wav.totalAudioLength / wav.frameSizeInBytes
                    val sa = SourceAudio(temp, 0, size)
                    return sa
                }
                val inputStream = rc.accessor.getInputStream(path)
                val extension = File(path).extension
                val temp = File.createTempFile("source", ".$extension")
                cache[path] = temp
                temp.deleteOnExit()
                inputStream.copyTo(temp.outputStream())
                val wav = WavFile(temp)
                val size = wav.totalAudioLength / wav.frameSizeInBytes
                SourceAudio(temp, 0, size)
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getChunk(chapter: Int, chunk: Int): SourceAudio? {
        val file = getChapter(chapter)?.file
        if (file != null) {
            val wav = WavFile(file)
            val cues = wav.metadata.getCues()
            cues.sortedBy { it.location }
            val index = chunk - 1
            if (cues.size > index) {
                val start = cues[index].location
                val end = if (cues.size > chunk) cues[chunk].location else wav.totalAudioLength / wav.frameSizeInBytes
                return SourceAudio(file, start, end)
            }
        }
        return null
    }
}
