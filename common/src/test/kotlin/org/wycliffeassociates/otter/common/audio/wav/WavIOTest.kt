package org.wycliffeassociates.otter.common.audio.wav

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class WavIOTest {
    private val testCues = listOf(
        WavCue(123_943_347,"marker 1"),
        WavCue(200_000_000,"marker 2"),
        WavCue(300_000_000, "marker 3 ")
    )

    @Test
    fun `test writing a file produces proper header`() {
        val temp = File.createTempFile("testwav", "wav")
        temp.deleteOnExit()
        val wav = WavFile(temp, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
        WavOutputStream(wav).use {
            it.write(1)
            it.write(2)
            it.write(3)
            it.write(4)
        }
        wav.update()
        assertEquals("Audio size:", 4, wav.totalAudioLength)
        assertEquals("Total size:", temp.length().toInt() - CHUNK_HEADER_SIZE, wav.totalDataLength)
        assertEquals("Metadata exists:", false, wav.hasMetadata)
        assertEquals("Metadata size:", 0, wav.metadata.totalSize)
        temp.delete()
    }

    @Test
    fun `test writing a metadata produces proper header`() {
        val temp = File.createTempFile("testwav", "wav")
        temp.deleteOnExit()
        val wav = WavFile(temp, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
        WavOutputStream(wav).use {
            it.write(1)
            it.write(2)
            it.write(3)
            it.write(4)
        }
        assertEquals("Data size", 36 + wav.totalAudioLength, wav.totalDataLength)

        wav.metadata.addCue(testCues[0].location, testCues[0].label)
        wav.metadata.addCue(testCues[1].location, testCues[1].label)
        wav.update()

        assertEquals("Audio size:", 4, wav.totalAudioLength)
        assertEquals("Data size:", 36 + wav.totalAudioLength + wav.metadata.totalSize, wav.totalDataLength)
        assertEquals("Metadata exists:", true, wav.hasMetadata)
        assertEquals("Metadata size:", 112, wav.metadata.totalSize)

        var cues = wav.metadata.getCues()
        assertEquals("Cue count:", 2, cues.size)
        assertEquals("Cue 1 label", testCues[0].label, cues[0].label)
        assertEquals("Cue 2 label", testCues[1].label, cues[1].label)

        assertEquals("Cue 1 location", testCues[0].location, cues[0].location)
        assertEquals("Cue 2 location", testCues[1].location, cues[1].location)

        wav.metadata.addCue(testCues[2].location, testCues[2].label)
        cues = wav.metadata.getCues()
        assertEquals("Cue count:", 3, cues.size)
        assertEquals("Cue 1 label:", testCues[0].label, cues[0].label)
        assertEquals("Cue 2 label:", testCues[1].label, cues[1].label)
        assertEquals("Cue 3 label:", testCues[2].label, cues[2].label)

        assertEquals("Cue 1 location:", testCues[0].location, cues[0].location)
        assertEquals("Cue 2 location:", testCues[1].location, cues[1].location)
        assertEquals("Cue 3 location:", testCues[2].location, cues[2].location)
        wav.update()

        assertEquals("Audio size:", 4, wav.totalAudioLength)
        assertEquals("Metadata exists:", true, wav.hasMetadata)
        assertEquals("Metadata size:", 160, wav.metadata.totalSize)
        assertEquals("Total size:", temp.length().toInt() - CHUNK_HEADER_SIZE, wav.totalDataLength)
        temp.delete()
    }
}
