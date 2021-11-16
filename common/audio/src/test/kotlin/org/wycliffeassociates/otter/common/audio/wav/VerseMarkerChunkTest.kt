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
package org.wycliffeassociates.otter.common.audio.wav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import org.wycliffeassociates.otter.common.audio.AudioCue

class TestData(val initial: List<AudioCue>, val result: List<AudioCue>)

class VerseMarkerChunkTest {

    val testEnv = listOf(
        TestData(
            listOf(
                AudioCue(1, "1"),
                AudioCue(2, "2"),
                AudioCue(3, "3")
            ),
            listOf(
                AudioCue(1, "1"),
                AudioCue(2, "2"),
                AudioCue(3, "3"),
                AudioCue(1, "orature-vm-1"),
                AudioCue(2, "orature-vm-2"),
                AudioCue(3, "orature-vm-3")
            )
        ),
        // locations out of order
        TestData(
            listOf(
                AudioCue(2, "2"),
                AudioCue(1, "1"),
                AudioCue(3, "3")
            ),
            listOf(
                AudioCue(2, "2"),
                AudioCue(1, "1"),
                AudioCue(3, "3"),
                AudioCue(2, "orature-vm-2"),
                AudioCue(1, "orature-vm-1"),
                AudioCue(3, "orature-vm-3")
            )
        ),
        // requiring padding to get to double word aligned
        TestData(
            listOf(
                AudioCue(2, "1"),
                AudioCue(1, "12"),
                AudioCue(3, "123"),
                AudioCue(4, "1234")
            ),
            listOf(
                AudioCue(2, "1"),
                AudioCue(1, "12"),
                AudioCue(3, "123"),
                AudioCue(4, "1234"),
                AudioCue(2, "orature-vm-1"),
                AudioCue(1, "orature-vm-12"),
                AudioCue(3, "orature-vm-123"),
                AudioCue(4, "orature-vm-1234")
            )
        ),
        TestData(
            listOf(
                AudioCue(0, "    "),
                AudioCue(2, "Verse 2"),
                AudioCue(3, "Marker 3   "),
                AudioCue(4, "   Verse 4"),
                AudioCue(Int.MAX_VALUE, " stuff5 ")
            ),
            listOf(
                AudioCue(0, "    "),
                AudioCue(2, "Verse 2"),
                AudioCue(3, "Marker 3   "),
                AudioCue(4, "   Verse 4"),
                AudioCue(Int.MAX_VALUE, " stuff5 "),
                AudioCue(2, "orature-vm-2"),
                AudioCue(3, "orature-vm-3"),
                AudioCue(4, "orature-vm-4"),
                AudioCue(Int.MAX_VALUE, "orature-vm-5")
            )
        ),
        // Test that only orature-vm markers are interpreted as verse markers
        TestData(
            listOf(
                AudioCue(0, "orature-vm-1"),
                AudioCue(2, "Verse 2"),
                AudioCue(3, "Marker 3   "),
                AudioCue(4, "   Verse 4"),
                AudioCue(Int.MAX_VALUE, " stuff5 ")
            ),
            listOf(
                AudioCue(0, "orature-vm-1"),
                AudioCue(2, "Verse 2"),
                AudioCue(3, "Marker 3   "),
                AudioCue(4, "   Verse 4"),
                AudioCue(Int.MAX_VALUE, " stuff5 "),
            )
        ),
        // Test that only lone digits are interpreted as verse markers
        TestData(
            listOf(
                AudioCue(0, "123"),
                AudioCue(2, "Verse 2"),
                AudioCue(3, "Marker 3   "),
                AudioCue(4, "   Verse 4"),
                AudioCue(Int.MAX_VALUE, " stuff5 ")
            ),
            listOf(
                AudioCue(0, "123"),
                AudioCue(0, "orature-vm-123"),
                AudioCue(2, "Verse 2"),
                AudioCue(3, "Marker 3   "),
                AudioCue(4, "   Verse 4"),
                AudioCue(Int.MAX_VALUE, " stuff5 "),
            )
        ),
        TestData(
            listOf(
                AudioCue(0, "1"),
                AudioCue(12, "2 "),
                AudioCue(149, " 3"),
                AudioCue(259, " 4 "),
                AudioCue(1000, "\t5"),
                AudioCue(2450, "6\n"),
                AudioCue(3212, " 7\t"),
                AudioCue(4112, " 8\t"),
                AudioCue(5112, " 9\t\n"),
            ),
            listOf(
                AudioCue(0, "1"),
                AudioCue(12, "2 "),
                AudioCue(149, " 3"),
                AudioCue(259, " 4 "),
                AudioCue(1000, "\t5"),
                AudioCue(2450, "6\n"),
                AudioCue(3212, " 7\t"),
                AudioCue(4112, " 8\t"),
                AudioCue(5112, " 9\t\n"),
                AudioCue(0, "orature-vm-1"),
                AudioCue(12, "orature-vm-2"),
                AudioCue(149, "orature-vm-3"),
                AudioCue(259, "orature-vm-4"),
                AudioCue(1000, "orature-vm-5"),
                AudioCue(2450, "orature-vm-6"),
                AudioCue(3212, "orature-vm-7"),
                AudioCue(4112, "orature-vm-8"),
                AudioCue(5112, "orature-vm-9"),
            )
        ),
        TestData(
            listOf(AudioCue(10, "verse 10")),
            listOf(AudioCue(10, "verse 10"), AudioCue(10, "orature-vm-10"))
        )
    )

    @Test
    fun writeCues() {
        val wavLengths = listOf(0, 3, 100, 400000)
        for (writeSize in wavLengths) {
            for (test in testEnv) {
                val file = File.createTempFile("test", "wav")
                file.deleteOnExit()

                // Creates a wav file with cue chunks to write freeform cue markers
                val wav = WavFile(file, 1, 44100, 16, WavMetadata(listOf(CueChunk())))
                for (cue in test.initial) {
                    wav.metadata.addCue(cue.location, cue.label)
                }
                val os = WavOutputStream(wav)
                os.use {
                    os.write(ByteArray(writeSize))
                }

                // Open another wav file that uses VerseMarkerChunk rather than CueChunk to find
                // verse markers from possible cue chunks
                val updateMetadata = WavFile(file)
                // write out the interpreted orature-vm markers
                WavOutputStream(updateMetadata, true).use {}

                // open the file again with CueChunk to verify that the original markers are still there
                val validator = WavFile(file, WavMetadata(listOf(CueChunk())))
                val resultMetadata = validator.metadata
                assertEquals(test.result.size, resultMetadata.getCues().size)
                for (cue in test.result) {
                    assertTrue(resultMetadata.getCues().contains(cue))
                }
            }
        }
    }
}
