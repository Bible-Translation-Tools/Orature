/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import java.util.*

/** The kinds of [Content] elements */
enum class ContentType {
    /** ContentType of primary [Content] elements. */
    TEXT,

    /**
     * ContentType of summations or secondary representations of other [Content]s, for example the complete rendering of
     * a chapter that is primarily represented as individual verses.
     */
    META,

    /**
     * The ContentType for titles within a reference or supplemental work, typically paired with (and followed by) a
     * single element of type [BODY].
     */
    TITLE,

    /**
     * The ContentType for detail within a reference or supplemental work, always (so far) paired with (preceded by)
     * one or more elements of type [TITLE].
     */
    BODY;

    companion object {
        fun of(s: String): ContentType? {
            val lower = s.toLowerCase()
            return values().firstOrNull { lower == it.name.toLowerCase() }
        }
    }
}

/** The set of ContentTypes for primary content. Only contains [ContentType.TEXT]. */
val primaryContentTypes: Set<ContentType> = EnumSet.of(ContentType.TEXT)

/**
 * The set of ContentTypes for content that is itself a reference or resource to other content. Contains
 * [ContentType.TITLE] and [ContentType.BODY].
 */
val helpContentTypes: Set<ContentType> = EnumSet.of(ContentType.TITLE, ContentType.BODY)
