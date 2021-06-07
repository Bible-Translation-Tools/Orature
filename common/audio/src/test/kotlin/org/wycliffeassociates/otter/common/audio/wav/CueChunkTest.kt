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
