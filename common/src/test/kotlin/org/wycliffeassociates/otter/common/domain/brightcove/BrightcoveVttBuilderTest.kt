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
import org.junit.Assert.assertTrue
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import java.nio.file.Files

class BrightcoveVttBuilderTest {

    @Test
    fun buildsCueIdsAndBridgeText() {
        val builder = BrightcoveVttBuilder()
        val content = listOf(
            content(1, "In the beginning"),
            content(2, "God"),
            content(3, "created"),
            content(4, "the heavens")
        )
        val markers = listOf(
            VerseMarker(1, 3, 0),
            VerseMarker(4, 4, 44100)
        )

        val cues = builder.buildCues(
            bookCode = "gen",
            chapterNumber = 1,
            verseMarkers = markers,
            totalFrames = 88200,
            chapterContent = content
        )

        assertEquals(2, cues.size)
        assertEquals("GEN 1:1-3", cues[0].id)
        assertEquals("In the beginning God created", cues[0].text)
        assertEquals(0, cues[0].startTimeUs)
        assertEquals(1_000_000, cues[0].endTimeUs)
        assertEquals("GEN 1:4", cues[1].id)
        assertEquals("the heavens", cues[1].text)
        assertEquals(1_000_000, cues[1].startTimeUs)
        assertEquals(2_000_000, cues[1].endTimeUs)
    }

    @Test
    fun writesVttWithBurritoTimestampFormat() {
        val builder = BrightcoveVttBuilder()
        val cues = listOf(
            BrightcoveVttCue(
                id = "MAT 1:1",
                startTimeUs = 0,
                endTimeUs = 1_000_000,
                text = "Verse one"
            )
        )

        val tempFile = Files.createTempFile("brightcove", ".vtt").toFile()
        try {
            builder.writeVtt(tempFile, cues)
            val output = tempFile.readText()
            assertTrue(output.contains("00:00:00.000 --> 00:00:01.000"))
            assertTrue(output.contains("MAT 1:1"))
            assertTrue(output.contains("Verse one"))
        } finally {
            tempFile.delete()
        }
    }

    private fun content(verse: Int, text: String): Content {
        return Content(
            sort = verse,
            labelKey = "verse",
            start = verse,
            end = verse,
            text = text,
            format = "usfm",
            type = ContentType.TEXT,
            draftNumber = 0
        )
    }
}
