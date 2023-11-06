package org.wycliffeassociates.otter.common.domain.content

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.wycliffeassociates.otter.common.data.Chunkification
import java.io.File
import javax.inject.Inject
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor

class ResetChunks @Inject constructor() {
    fun resetChapter(accessor: ProjectFilesAccessor, chapter: Chapter) {
        markTakesForDeletion(chapter)
        chapter.reset().blockingAwait()
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

        val mapper = ObjectMapper(JsonFactory()).registerModule(KotlinModule())
        val chunks: Chunkification = mapper.readValue(chunkFile)
        chunks.remove(chapterNumber)

        chunkFile.writer().use {
            mapper.writeValue(it, chunks)
        }
    }
}
