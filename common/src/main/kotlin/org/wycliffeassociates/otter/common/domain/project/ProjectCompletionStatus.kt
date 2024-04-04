package org.wycliffeassociates.otter.common.domain.project

import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.narration.ChapterRepresentation
import javax.inject.Inject

class ProjectCompletionStatus @Inject constructor() {

    fun getChapterNarrationProgress(workbook: Workbook, chapter: Chapter): Double {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        chapterRepresentation.loadFromSerializedVerses()

        return chapterRepresentation.getCompletionProgress()
    }

    fun getChapterTranslationProgress(chapter: Chapter): Double {
        val chunkCount = chapter.chunkCount.blockingGet()

        if (chunkCount == 0) return 0.0

        val chunkWithAudio = chapter.chunks
            .map {
                it.count { it.hasSelectedAudio() }
            }
            .blockingGet()

        return chunkWithAudio.toDouble() / chunkCount
    }
}