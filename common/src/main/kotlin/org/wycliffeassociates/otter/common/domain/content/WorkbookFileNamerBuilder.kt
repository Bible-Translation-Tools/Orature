package org.wycliffeassociates.otter.common.domain.content

import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook

object WorkbookFileNamerBuilder {
    fun createFileNamer(
        workbook: Workbook,
        chapter: Chapter,
        chunk: Chunk?,
        recordable: Recordable,
        rcSlug: String
    ) = FileNamer(
        bookSlug = workbook.target.slug,
        languageSlug = workbook.target.language.slug,
        chapterCount = workbook.target.chapters.count().blockingGet(),
        chapterTitle = chapter.title,
        chapterSort = chapter.sort,
        chunkCount = chapter.chunks.count().blockingGet(),
        start = chunk?.start,
        end = chunk?.end,
        contentType = recordable.contentType,
        sort = recordable.sort,
        rcSlug = rcSlug
    )
}
