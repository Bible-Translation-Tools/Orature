/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

class InsertTakeAudio @Inject constructor() {

    fun insertChunksAudio(
        workbook: Workbook,
        chapter: Chapter,
        chunkFiles: Map<String, File>
    ): Completable {
        return Completable.fromCallable {
            val chunks = chapter.chunks.getValues(emptyArray())

            chunkFiles
                .mapKeys { findChunk(it.key, chunks) }
                .filterKeys { it != null }
                .forEach {
                    val chunk = it.key!!
                    val file = it.value

                    insertAudio(workbook, chapter, chunk, chunk, file)
                }
        }
    }

    fun insertChapterAudio(
        workbook: Workbook,
        chapter: Chapter,
        chapterFile: File
    ): Completable {
        return Completable.fromCallable {
            insertAudio(workbook, chapter, null, chapter, chapterFile)
        }
    }

    private fun insertAudio(
        workbook: Workbook,
        chapter: Chapter,
        chunk: Chunk?,
        recordable: Recordable,
        audioFile: File
    ) {
        val namer = WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbook,
            chapter = chapter,
            chunk = chunk,
            recordable = recordable,
            rcSlug = workbook.source.resourceMetadata.identifier
        )

        val format = AudioFileFormat.of(audioFile.extension)
        val filename = namer.generateName(1, format)

        val chapterDir = getChapterAudioDirectory(
            workbook.projectFilesAccessor.audioDir,
            namer.formatChapterNumber()
        )

        val takeFile = chapterDir.resolve(File(filename))

        audioFile.inputStream().use { input ->
            takeFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val newTake = Take(
            name = takeFile.name,
            file = takeFile,
            number = 1,
            format = MimeType.WAV,
            createdTimestamp = LocalDate.now()
        )

        recordable.audio.insertTake(newTake)
    }

    private fun getChapterAudioDirectory(projectAudioDir: File, chapterNum: String): File {
        val chapterAudioDir = projectAudioDir.resolve(chapterNum)
        chapterAudioDir.mkdirs()
        return chapterAudioDir
    }

    private fun findChunk(title: String, chunks: Array<Chunk>): Chunk? {
        return chunks.singleOrNull { it.title == title }
    }
}