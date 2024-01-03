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
package org.wycliffeassociates.otter.common.domain.audio

import org.junit.Assert.assertEquals
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import java.io.File

class SourceAudioFileTest {
    val testEnv =
        listOf(
            listOf(
                AudioCue(1, "1"),
                AudioCue(2, "2"),
                AudioCue(3, "3"),
            ),
            // locations out of order
            listOf(
                AudioCue(2, "2"),
                AudioCue(1, "1"),
                AudioCue(3, "3"),
            ),
            // requiring padding to get to double word aligned
            listOf(
                AudioCue(2, "1"),
                AudioCue(1, "12"),
                AudioCue(3, "123"),
                AudioCue(4, "1234"),
            ),
            // labels have various whitespace, location range from 0 to max
            listOf(
                AudioCue(0, "    "),
                AudioCue(2, "Verse 1"),
                AudioCue(3, "Verse 1   "),
                AudioCue(4, "   Verse 1"),
                AudioCue(Int.MAX_VALUE, "         "),
            ),
            // the existence of an Orature marker here implies the extra markers should not be parsed as a verse
            // because a valid verse would have been written out as orature-vm-# like the chunk marker
            listOf(
                AudioCue(0, "orature-chunk-1"),
                AudioCue(2, "Verse 1"),
                AudioCue(3, "2"),
                AudioCue(4, "   Verse 1"),
                AudioCue(Int.MAX_VALUE, "         "),
            ),
            listOf(
                AudioCue(0, "orature-chunk-1"),
                AudioCue(2, "orature-vm-1"),
                AudioCue(3, "2"),
                AudioCue(4, "   Verse 1"),
                AudioCue(Int.MAX_VALUE, "         "),
            ),
        )

    data class TestResult(val verses: Int, val chunks: Int)

    val results =
        arrayOf(
            TestResult(3, 0),
            TestResult(3, 0),
            TestResult(4, 0),
            TestResult(3, 0),
            TestResult(0, 1),
            TestResult(1, 1),
        )

    @Test
    fun testCreateCues() {
        for ((i, testCues) in testEnv.withIndex()) {
            val temp = File.createTempFile("test", ".wav").apply { deleteOnExit() }
            val af = AudioFile(temp, 1, 41000, 16, WavMetadata(listOf(CueChunk())))
            testCues.forEach {
                af.addCue(it.location, it.label)
            }
            af.update()

            val saf = OratureAudioFile(temp)
            assertEquals(results[i].verses, saf.getMarker<VerseMarker>().size)
            assertEquals(results[i].chunks, saf.getMarker<ChunkMarker>().size)
        }
    }
}
