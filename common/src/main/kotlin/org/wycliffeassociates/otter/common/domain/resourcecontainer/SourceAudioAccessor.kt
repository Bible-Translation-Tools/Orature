package org.wycliffeassociates.otter.common.domain.resourcecontainer

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import java.io.File
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider

data class SourceAudio(val file: File, val start: Int, val end: Int)

class SourceAudioAccessor(
    val directoryProvider: IDirectoryProvider,
    val metadata: ResourceMetadata,
    val project: String
) {

    private val dir = File(directoryProvider.cacheDirectory, "source").apply { mkdirs() }
    private val cache = mutableMapOf<String, File>()

    fun getChapter(chapter: Int): SourceAudio? {
        ResourceContainer.load(metadata.path).use { rc ->
            if (rc.media != null) {
                val mediaProject = rc.media!!.projects.find { it.identifier == project }
                var media = mediaProject?.media?.find { it.identifier == "mp3" }
                val cue = mediaProject?.media?.find { it.identifier == "cue" }
                if (media == null || cue == null) {
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
                if (cache.containsKey(path)) {
                    cache[path]
                }
                val extension = File(path).extension
                val temp = File(dir, File(path).name).apply { createNewFile() }
                if (extension == "mp3") {
                    val cueFile = File(temp.absolutePath.replace(".mp3", ".cue")).apply { createNewFile() }
                    cueFile.deleteOnExit()
                    val cuePath = path.replace(".mp3", ".cue")
                    rc.accessor.getInputStream(cuePath).use {
                        it.copyTo(cueFile.outputStream())
                    }
                }
                cache[path] = temp
                temp.deleteOnExit()
                rc.accessor.getInputStream(path).use { it.copyTo(temp.outputStream()) }
                val audioFile = AudioFile(temp)
                val size = audioFile.totalFrames
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
            val audioFile = AudioFile(file)
            val cues = audioFile.metadata.getCues()
            cues.sortedBy { it.location }
            val index = chunk - 1
            if (cues.size > index) {
                val start = cues[index].location
                val end = if (cues.size > chunk) cues[chunk].location else audioFile.totalFrames
                return SourceAudio(file, start, end)
            }
        }
        return null
    }
}
