package org.wycliffeassociates.otter.common.versification

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.Book

interface Versification {
    fun getVersesInChapter(bookSlug: String, chapterNumber: Int): Int

    fun getVersesInChapter(book: Collection, chapterNumber: Int): Int {
        return getVersesInChapter(book.slug, chapterNumber)
    }

    fun getVersesInChapter(book: Book, chapterNumber: Int): Int {
        return getVersesInChapter(book.slug, chapterNumber)
    }
}