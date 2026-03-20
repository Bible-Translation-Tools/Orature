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
package org.wycliffeassociates.otter.common.domain.brightcove

class BrightcoveMetadataBuilder {

    fun buildVideoRequest(
        languageCode: String,
        resourceType: String,
        canonicalOrder: Int,
        bookCode: String,
        localizedBookName: String,
        chapter: Int,
        countryInfo: CountryInfo,
        cuePoints: List<BrightcoveCuePoint> = emptyList()
    ): BrightcoveVideoRequest {
        val chapterLabel2 = chapter.toString().padStart(2, '0')
        val chapterLabel3 = chapter.toString().padStart(3, '0')

        val normalizedBookCode = bookCode.lowercase()
        val normalizedLanguage = languageCode.lowercase()
        val normalizedResourceType = resourceType.trim().lowercase()
        val videoName = "${normalizedLanguage}_${canonicalOrder}-${normalizedBookCode}_${chapterLabel2}"
        val referenceId = "${normalizedLanguage}_${normalizedResourceType}_${normalizedBookCode}_${chapterLabel3}"

        val customFields = linkedMapOf(
            "book" to normalizedBookCode,
            "canonical_order" to canonicalOrder.toString(),
            "chapter" to chapterLabel3,
            "language_code" to normalizedLanguage,
            "localized_book_name" to localizedBookName
        )

        countryInfo.code?.trim()?.takeIf { it.isNotEmpty() }?.let { code ->
            customFields["country"] = code
        }

        val tags = mutableListOf<String>()
        tags.add(normalizedLanguage)
        tags.add("${canonicalOrder}-${normalizedBookCode}")
        tags.add("language:${normalizedLanguage}")
        tags.add("book:${normalizedBookCode}")
        tags.add("chapter:${chapter}")

        countryInfo.code?.trim()?.takeIf { it.isNotEmpty() }?.let { code ->
            tags.add("country:${code.lowercase()}")
        }
        countryInfo.name?.trim()?.takeIf { it.isNotEmpty() }?.let { name ->
            tags.add(name.lowercase())
        }

        return BrightcoveVideoRequest(
            name = videoName,
            referenceId = referenceId,
            tags = tags.distinct(),
            customFields = customFields,
            cuePoints = cuePoints
        )
    }
}
