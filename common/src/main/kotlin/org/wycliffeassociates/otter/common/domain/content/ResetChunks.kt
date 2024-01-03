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
package org.wycliffeassociates.otter.common.domain.content

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.wycliffeassociates.otter.common.data.Chunkification
import java.io.File
import javax.inject.Inject
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor

class ResetChunks @Inject constructor() {
    fun resetChapter(accessor: ProjectFilesAccessor, chapter: Chapter): Completable {
        return Completable
            .fromAction {
                markTakesForDeletion(chapter)
            }
            .andThen(chapter.reset())
            .subscribeOn(Schedulers.io())
    }

    private fun deleteChunkedSourceAudio(accessor: ProjectFilesAccessor, chapter: Chapter) {
        accessor.sourceAudioDir.listFiles()?.forEach {  file ->
            if(chapterMatches(file, chapter.sort)) {
                file.delete()
            }
        }
    }

    private fun chapterMatches(file: File, chapter: Int): Boolean {
        return file.name.contains(Regex("_c(0*)$chapter\\."))
    }

    private fun markTakesForDeletion(chapter: Chapter) {
        chapter.chunks.value
            ?.forEach { chunk ->
                chunk.draftNumber = -1
                chunk.audio.getAllTakes()
                    .filter { it.deletedTimestamp.value?.value == null }
                    .forEach { take ->
                        take.deletedTimestamp.accept(DateHolder.now())
                    }
            }
    }

    private fun removeChapterFromChunkFile(chunkFile: File, chapterNumber: Int) {
        if (!chunkFile.exists() || chunkFile.length() == 0L) {
            return
        }

        val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
        val chunks: Chunkification = mapper.readValue(chunkFile)
        chunks.remove(chapterNumber)

        chunkFile.writer().use {
            mapper.writeValue(it, chunks)
        }
    }
}
