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

    fun getVersesInChapter(bookSlug: String, chapterNumber: Int): Int

    fun getVersesInChapter(book: Collection, chapterNumber: Int): Int {
        return getVersesInChapter(book.slug, chapterNumber)
    }

    fun getVersesInChapter(book: Book, chapterNumber: Int): Int {
        return getVersesInChapter(book.slug, chapterNumber)
    }

    fun getBookSlugs(): List<String>
}