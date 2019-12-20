package org.wycliffeassociates.otter.common.io.wav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer

class CueChunkTest {

    val testEnv = listOf(
        listOf(
            WavCue(1, "1"),
            WavCue(2, "2"),
            WavCue(3, "3")
        ),
        // locations out of order
        listOf(
            WavCue(2, "2"),
            WavCue(1, "1"),
            WavCue(3, "3")
        ),
        // requiring padding to get to double word aligned
        listOf(
            WavCue(2, "1"),
            WavCue(1, "12"),
            WavCue(3, "123"),
            WavCue(4, "1234")
        ),
        // labels have various whitespace, location range from 0 to max
        listOf(
            WavCue(0, "    "),
            WavCue(2, "Verse 1"),
            WavCue(3, "Verse 1   "),
            WavCue(4, "   Verse 1"),
            WavCue(Int.MAX_VALUE, "         ")
        )
    )

    @Test
    fun testCreateCues() {
        for (testCues in testEnv) {
            val cues = CueChunk()
            for (cue in testCues) {
                cues.addCue(cue)
            }
            val outArray = cues.create()
            cues.parse(ByteBuffer.wrap(outArray))

            val outCues = cues.cues

            assertEquals(testCues.size, outCues.size)
            for (cue in testCues) {
                assertEquals(true, outCues.contains(cue))
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
                val wav = WavFile(file, 1, 44100, 16)
                for (cue in cues) {
                    wav.metadata.addCue(cue.location, cue.label)
                }
                val os = WavOutputStream(wav)
                os.use {
                    os.write(ByteArray(writeSize))
                }
                val validator = WavFile(file)
                val resultMetadata = validator.metadata
                assertEquals(cues.size, resultMetadata.getCues().size)
                for (cue in cues) {
                    assertTrue(resultMetadata.getCues().contains(cue))
                }
            }
        }
    }
}