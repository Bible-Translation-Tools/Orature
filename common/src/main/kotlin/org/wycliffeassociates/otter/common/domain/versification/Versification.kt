package org.wycliffeassociates.otter.common.domain.versification

import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.Book

interface Versification {
    fun getChaptersInBook(bookSlug: String): Int

    fun getChaptersInBook(book: Book): Int {
        return getChaptersInBook(book.slug)
    }

    fun getChaptersInBook(book: Collection): Int {
        return getChaptersInBook(book.slug)
    }

    fun getVersesInChapter(
        bookSlug: String,
        chapterNumber: Int,
    ): Int

    fun getVersesInChapter(
        book: Collection,
        chapterNumber: Int,
    ): Int {
        return getVersesInChapter(book.slug, chapterNumber)
    }

    fun getVersesInChapter(
        book: Book,
        chapterNumber: Int,
    ): Int {
        return getVersesInChapter(book.slug, chapterNumber)
    }

    fun getBookSlugs(): List<String>
}
