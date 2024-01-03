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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Data class representing the json form of paratext vrs Versification files
 * https://github.com/ubsicap/versification_json/blob/master/versification_as_json.schema.json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ParatextVersification(
    val basedOn: String?,
    val maxVerses: MaxVerses,
    val mappedVerses: MappedVerses?,
    val excludedVerses: List<String>?,
    val partialVerses: PartialVerses?
): Versification {

    override fun getChaptersInBook(bookSlug: String): Int {
        return maxVerses[bookSlug]?.size ?: 0
    }

    override fun getVersesInChapter(bookSlug: String, chapterNumber: Int): Int {
        return maxVerses[bookSlug]?.get(chapterNumber-1)?.toInt() ?: 0
    }

    override fun getBookSlugs(): List<String> {
        return maxVerses.keys.toList()
    }
}

typealias MaxVerses = Map<String, List<String>>

typealias MappedVerses = Map<String, String>

typealias PartialVerses = Map<String, List<String>>