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
package org.wycliffeassociates.otter.common.data.primitives

@Deprecated(
    """
        "Don't use this to figure out labels to use for UI, as text could be verse or chunk.
         Book elements already have a label field which should be used directly. This enum should be removed.
"""
)
enum class ContentLabel(val value: String, val type: ContentType) {
    CHAPTER("chapter", ContentType.META),
    VERSE("verse", ContentType.TEXT),
    HELP_TITLE("title", ContentType.TITLE),
    HELP_BODY("body", ContentType.BODY);

    companion object {
        fun of(contentType: ContentType) = when (contentType) {
            ContentType.META -> CHAPTER
            ContentType.TEXT -> VERSE
            ContentType.TITLE -> HELP_TITLE
            ContentType.BODY -> HELP_BODY
        }
    }
}
