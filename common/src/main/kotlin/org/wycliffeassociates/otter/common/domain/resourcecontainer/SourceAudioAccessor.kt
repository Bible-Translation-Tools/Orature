/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.resourcecontainer

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import java.io.File
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.audio.OratureCueType
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.InputStream

data class SourceAudio(val file: File, val start: Int, val end: Int)

class SourceAudioAccessor(
    val directoryProvider: IDirectoryProvider,
    val metadata: ResourceMetadata,
    val project: String
) {

    private val logger = LoggerFactory.getLogger(SourceAudioAccessor::class.java)

    private val dir = File(directoryProvider.cacheDirectory, "source").apply { mkdirs() }
    private val cache = mutableMapOf<String, File>()

    fun getChapter(chapter: Int, target: Book? = null): SourceAudio? {
        logger.info("Looking for target audio for chapter: $chapter with book: $project")

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
        logger.info("No source audio found")
        return null
    }

    fun getUserMarkedChapter(chapter: Int, target: Book? = null): SourceAudio? {
        logger.info("Looking for source audio (chunks) for chapter: $chapter with target book: $target")
        return target?.let { target ->
            val accessor = ProjectFilesAccessor(directoryProvider, metadata, target.resourceMetadata, target)
            val dir = accessor.sourceAudioDir
            val file = dir.listFiles()?.find {
                chapterMatches(it, chapter) && validAudioExtension(it)
            }
            file?.let {
                logger.info("Found the source audio file! ${it.path}")
                val oratureAudioFile = OratureAudioFile(it)
                val size = oratureAudioFile.totalFrames
                SourceAudio(it, 0, size)
            }
        }
    }

    fun deleteAudio() {
        metadata.path
            .walk()
            .find {
                it.invariantSeparatorsPath.endsWith("${RcConstants.SOURCE_MEDIA_DIR}/${project}")
            }
            ?.run {
                deleteRecursively()
            }
    }

    private fun getChapter(
        media: Media,
        chapter: Int,
        rc: ResourceContainer
    ): SourceAudio? {
        return if (rc.media != null && media.chapterUrl.isNotEmpty()) {
            val path = media.chapterUrl.replace("{chapter}", chapter.toString())
            if (rc.accessor.fileExists(path)) {
                val file = when (cache.containsKey(path)) {
                    true -> cache[path]!!
                    false -> {
                        val temp = File(dir, File(path).name).apply { createNewFile() }
                        val extension = File(path).extension
                        if (extension == "mp3") {
                            val cueFile = File(temp.absolutePath.replace(".mp3", ".cue"))
                                .apply { createNewFile() }
                            cueFile.deleteOnExit()
                            val cuePath = path.replace(".mp3", ".cue")
                            rc.accessor.getInputStream(cuePath).use { input ->
                                cueFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                        cache[path] = temp
                        temp.deleteOnExit()
                        rc.accessor.getInputStream(path).use { input ->
                            temp.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        temp
                    }
                }

                val oratureAudioFile = OratureAudioFile(file)
                val size = oratureAudioFile.totalFrames
                SourceAudio(file, 0, size)
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getChunk(chapter: Int, chunk: Int, verse: Int, target: Book?): SourceAudio? {
        val file = getUserMarkedChapter(chapter, target)?.file
        if (file != null) {
            val oratureAudioFile = OratureAudioFile(file)
            val chunks = oratureAudioFile
                .getMarker<ChunkMarker>()
                .sortedBy { it.location }
            val marker = chunks.find { it.chunk == chunk }
            if (marker != null) {
                val markerIndex = chunks.indexOf(marker)
                val nextMarker = if (chunks.lastIndex > markerIndex) chunks[markerIndex + 1] else null
                val start = marker.location
                val end = nextMarker?.location ?: oratureAudioFile.totalFrames
                return SourceAudio(file, start, end)
            }
        }
        return getVerse(chapter, verse, target)
    }

    private fun getVerse(chapter: Int, chunk: Int, target: Book?): SourceAudio? {
        val file = getChapter(chapter, target)?.file
        if (file != null) {
            val oratureAudioFile = OratureAudioFile(file)
            val verses = oratureAudioFile
                .getMarker<VerseMarker>()
                .sortedBy { it.location }
            val marker = verses.find { it.start == chunk }
            if (marker != null) {
                val markerIndex = verses.indexOf(marker)
                val nextMarker = if (verses.lastIndex > markerIndex) verses[markerIndex + 1] else null
                val start = marker.location
                val end = nextMarker?.location ?: oratureAudioFile.totalFrames
                return SourceAudio(file, start, end)
            }
        }
        return null
    }

    private fun chapterMatches(file: File, chapter: Int): Boolean {
        return file.name.contains(Regex("_c(0*)$chapter\\."))
    }

    private fun validAudioExtension(file: File): Boolean {
        return AudioFileFormat.values().map { it.extension }.contains(file.extension)
    }

    companion object {
        fun hasSourceAudio(
            metadata: ResourceMetadata,
            projectSlug: String
        ): Boolean {
            ResourceContainer.load(metadata.path).use { rc ->
                val mediaTemplatePaths = rc.media?.projects
                    ?.find { it.identifier == projectSlug }
                    ?.media
                    ?.filter { AudioFileFormat.isSupported(it.identifier) }
                    ?.map { it.chapterUrl } ?: return false

                val fileNameRegex = mediaTemplatePaths
                    .map { File(it).nameWithoutExtension.replace("{chapter}", "\\d{1,3}") }
                    .joinToString("|")
                    .let { Regex(it) }


                val pathsInRC = rc.accessor.list(RcConstants.SOURCE_MEDIA_DIR)
                val hasAudioFile = pathsInRC.any {
                    File(it).nameWithoutExtension.matches(fileNameRegex)
                }

                return hasAudioFile
            }
        }
    }
}
