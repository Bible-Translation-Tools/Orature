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
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class BrightcoveCuePointBuilderTest {

    @Test
    fun buildsCuePointsWithUppercaseReferenceAndRoundedSeconds() {
        val builder = BrightcoveCuePointBuilder()
        val markers = listOf(
            VerseMarker(1, 1, 0),
            VerseMarker(2, 3, 50_000)
        )

        val cuePoints = builder.buildCuePoints(
            bookCode = "mat",
            chapterNumber = 1,
            verseMarkers = markers
        )

        assertEquals(2, cuePoints.size)
        assertEquals("MAT 1:1", cuePoints[0].name)
        assertEquals(0.0, cuePoints[0].time, 0.0001)
        assertEquals("CODE", cuePoints[0].type)
        assertFalse(cuePoints[0].forceStop)

        assertEquals("MAT 1:2-3", cuePoints[1].name)
        assertEquals(1.1337, cuePoints[1].time, 0.0001)
        assertEquals("CODE", cuePoints[1].type)
        assertFalse(cuePoints[1].forceStop)
    }
}
