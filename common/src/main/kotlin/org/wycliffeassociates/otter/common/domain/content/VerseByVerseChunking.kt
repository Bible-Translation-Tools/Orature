package org.wycliffeassociates.otter.common.domain.content

import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor

class VerseByVerseChunking {
    fun chunkVerseByVerse(
        accessor: ProjectFilesAccessor,
        chunkCreator: (Int) -> Unit,
        projectSlug: String,
        chapterNumber: Int
    ) {
        accessor.getChapterText(projectSlug, chapterNumber).forEachIndexed { idx, str ->
            chunkCreator(idx + 1)
        }
    }
}
