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
package org.wycliffeassociates.otter.common.domain.content

import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import kotlin.IllegalStateException

class FileNamer(
    val start: Int? = null,
    val end: Int? = null,
    val sort: Int? = null,
    val contentType: ContentType,
    val languageSlug: String,
    val bookSlug: String,
    val rcSlug: String,
    val chunkCount: Long,
    val chapterCount: Long,
    val chapterTitle: String,
    val chapterSort: Int
) {
    init {
        checkStartLessThanEnd()
    }

    private fun checkStartLessThanEnd() {
        if (end != null && start == null)
            throw IllegalStateException("start should not be null if end is not null")
        if (start != null && end == null)
            throw IllegalStateException("end should not be null if start is not null")
        start?.let { s ->
            end?.let { e ->
                if (s > e)
                    throw IllegalStateException("start > end")
            }
        }
    }

    fun generateName(takeNumber: Int, format: AudioFileFormat): String {
        return listOfNotNull(
            languageSlug,
            rcSlug,
            bookSlug,
            formatChapterNumber(),
            formatVerseNumber(),
            formatSort(),
            formatContentType(),
            "t$takeNumber"
        ).joinToString("_", postfix = ".${format.extension}")
    }

    internal fun formatChapterNumber(): String {
        val chapterFormat = if (chapterCount > 99) "%03d" else "%02d"
        val chapterNum = chapterFormat.format(chapterTitle.toIntOrNull() ?: chapterSort)
        return "c$chapterNum"
    }

    internal fun formatVerseNumber(): String? {
        val verseFormat = if (chunkCount > 99) "%03d" else "%02d"
        val verseNum = when (start) {
            null -> null
            end -> verseFormat.format(start)
            else -> "$verseFormat-$verseFormat".format(start, end)
        }
        return verseNum?.let { "v$it" }
    }

    private fun formatContentType(): String? {
        return when (contentType) {
            ContentType.TEXT -> null
            else -> contentType.toString().toLowerCase()
        }
    }

    private fun formatSort(): String? {
        return when (contentType) {
            ContentType.TITLE, ContentType.BODY -> sort?.let { "s$it" }
            else -> null
        }
    }

    companion object {
        const val DEFAULT_RC_SLUG = "reg"
    }
}
