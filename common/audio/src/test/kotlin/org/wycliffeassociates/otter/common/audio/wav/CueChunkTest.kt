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
package org.wycliffeassociates.otter.common.audio.wav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import org.wycliffeassociates.otter.common.audio.AudioCue

class CueChunkTest {

    val testEnv = listOf(
        listOf(
            AudioCue(1, "1"),
            AudioCue(2, "2"),
            AudioCue(3, "3")
        ),
        // locations out of order
        listOf(
            AudioCue(2, "2"),
            AudioCue(1, "1"),
            AudioCue(3, "3")
        ),
        // requiring padding to get to double word aligned
        listOf(
            AudioCue(2, "1"),
            AudioCue(1, "12"),
            AudioCue(3, "123"),
            AudioCue(4, "1234")
        ),
        // labels have various whitespace, location range from 0 to max
        listOf(
            AudioCue(0, "    "),
            AudioCue(2, "Verse 1"),
            AudioCue(3, "Verse 1   "),
            AudioCue(4, "   Verse 1"),
            AudioCue(Int.MAX_VALUE, "         ")
        )
    )

    @Test
    fun testCreateCues() {
        for (testCues in testEnv) {
            val cues = CueChunk()
            for (cue in testCues) {
                cues.addCue(cue)
            }
            val outArray = cues.toByteArray()
            val outParser = CueChunk()
            outParser.parse(ByteBuffer.wrap(outArray))

            val outCues = outParser.cues

            assertEquals(testCues.size, outCues.size)
            for (cue in testCues) {
                assertTrue(outCues.contains(cue))
            }
        }
    }

    @Test
    fun writeCues() {
        val wavLengths = listOf(0, 3, 100, 400000)
        for (writeSize in wavLengths) {
            for (cues in testEnv) {
                val file = File.createTempFile("test", "wav")
                file.deleteOnExit()
                val wav = WavFile(file, 1, 44100, 16, WavMetadata(listOf(CueChunk())))
                for (cue in cues) {
                    wav.metadata.addCue(cue.location, cue.label)
                }
                val os = WavOutputStream(wav)
                os.use {
                    os.write(ByteArray(writeSize))
                }
                val validator = WavFile(file, WavMetadata(listOf(CueChunk())))
                val resultMetadata = validator.metadata
                assertEquals(cues.size, resultMetadata.getCues().size)
                for (cue in cues) {
                    assertTrue(resultMetadata.getCues().contains(cue))
                }
            }
        }
    }
}
