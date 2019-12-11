package org.wycliffeassociates.otter.jvm.markerapp.audio.wav

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer

class CueChunkTest {

    val testEnv = listOf(
        listOf(
            WavCue(1, "1"),
            WavCue(2, "2"),
            WavCue(3, "3")
        ),
        listOf( // locations out of order
            WavCue(2, "2"),
            WavCue(1, "1"),
            WavCue(3, "3")
        ),
        listOf( // requiring padding to get to double word aligned
            WavCue(2, "1"),
            WavCue(1, "12"),
            WavCue(3, "123"),
            WavCue(4, "1234")
        ),
        listOf( // labels have various whitespace, location range from 0 to max
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
            cues.parseMetadata(ByteBuffer.wrap(outArray))

            val outCues = cues.cues

            assertEquals(testCues.size, outCues.size)
            for (cue in testCues) {
                assertEquals(true, outCues.contains(cue))
            }
        }
    }
}