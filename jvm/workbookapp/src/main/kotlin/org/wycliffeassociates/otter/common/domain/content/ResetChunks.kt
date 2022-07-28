package org.wycliffeassociates.otter.common.domain.content

import java.io.File
import javax.inject.Inject
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor

class ResetChunks @Inject constructor() {
    fun resetChapter(accessor: ProjectFilesAccessor, chapter: Chapter) {
        markTakesForDeletion(chapter)
        deleteChunkedSourceAudio(accessor, chapter)
        chapter.reset()
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
        chapter.chunks.getValues(emptyArray()).forEach { chunk ->
            chunk.draftNumber = -1
            chunk.audio.getAllTakes()
                .filter { it.deletedTimestamp.value?.value == null }
                .forEach { take ->
                    take.deletedTimestamp.accept(DateHolder.now())
                }
        }
    }
}
