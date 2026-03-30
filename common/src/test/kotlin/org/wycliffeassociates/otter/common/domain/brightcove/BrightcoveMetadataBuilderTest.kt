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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrightcoveMetadataBuilderTest {

    @Test
    fun buildsRequiredFieldsAndTags() {
        val builder = BrightcoveMetadataBuilder()
        val request = builder.buildVideoRequest(
            languageCode = "bzs",
            resourceType = "ulb",
            canonicalOrder = 41,
            bookCode = "mat",
            localizedBookName = "Mateus",
            chapter = 5,
            countryInfo = CountryInfo("BR", "Brazil")
        )

        assertEquals("bzs_41-mat_05", request.name)
        assertEquals("bzs_ulb_audio_mat_005", request.referenceId)
        assertEquals(
            mapOf(
                "book" to "mat",
                "canonical_order" to "41",
                "chapter" to "005",
                "language" to "bzs",
                "localized_book_name" to "Mateus",
                "country" to "BR"
            ),
            request.customFields
        )

        assertTrue(request.tags.contains("bzs"))
        assertTrue(request.tags.contains("41-mat"))
        assertTrue(request.tags.contains("language:bzs"))
        assertTrue(request.tags.contains("book:mat"))
        assertTrue(request.tags.contains("chapter:5"))
        assertTrue(request.tags.contains("country:br"))
        assertTrue(request.tags.contains("brazil"))
    }

    @Test
    fun omitsCountryWhenMissing() {
        val builder = BrightcoveMetadataBuilder()
        val request = builder.buildVideoRequest(
            languageCode = "bzs",
            resourceType = "ulb",
            canonicalOrder = 41,
            bookCode = "mat",
            localizedBookName = "Mateus",
            chapter = 5,
            countryInfo = CountryInfo(null, null)
        )

        assertFalse(request.customFields.containsKey("country"))
        assertFalse(request.tags.any { it.startsWith("country:") })
        assertFalse(request.tags.contains("brazil"))
    }
}
