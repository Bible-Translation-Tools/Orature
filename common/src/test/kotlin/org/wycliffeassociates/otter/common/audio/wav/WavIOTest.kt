package org.wycliffeassociates.otter.common.audio.wav

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class WavIOTest {

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
        assertEquals("Audio size should be 4", 4, wav.totalAudioLength)
        assertEquals("Metadata exists:", false, wav.hasMetadata)
        assertEquals("Metadata size should be 0", 0, wav.metadata.totalSize)
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

        wav.metadata.addCue(123_943_347,"marker 1")
        wav.metadata.addCue(200_000_000,"marker 2")
        wav.update()
        assertEquals("Audio size", 4, wav.totalAudioLength)
        assertEquals("Data size", 36 + wav.totalAudioLength + wav.metadata.totalSize, wav.totalDataLength)
        assertEquals("Metadata exists:", true, wav.hasMetadata)
        assertEquals("Metadata size should be ", 112, wav.metadata.totalSize)

        var cues = wav.metadata.getCues()
        assertEquals("Cue count should be 2:", 2, cues.size)
        assertEquals("Cue 1 label is marker 1", "marker 1", cues[0].label)
        assertEquals("Cue 1 label is marker 2", "marker 2", cues[1].label)

        assertEquals("Cue 1 location is 123_943_347", 123_943_347, cues[0].location)
        assertEquals("Cue 1 location is 200_000_000", 200_000_000, cues[1].location)

        wav.metadata.addCue(300_000_000, "marker 3")
        cues = wav.metadata.getCues()
        assertEquals("Cue count should be 3:", 3, cues.size)
        assertEquals("Cue 1 label is marker 1", "marker 1", cues[0].label)
        assertEquals("Cue 2 label is marker 2", "marker 2", cues[1].label)
        assertEquals("Cue 3 label is marker 3", "marker 3", cues[2].label)

        assertEquals("Cue 1 location is 123_943_347", 123_943_347, cues[0].location)
        assertEquals("Cue 2 location is 200_000_000", 200_000_000, cues[1].location)
        assertEquals("Cue 3 location is 300_000_000", 300_000_000, cues[2].location)
        wav.update()

        assertEquals("Audio size should be 4", 4, wav.totalAudioLength)
        assertEquals("Metadata exists:", true, wav.hasMetadata)
        assertEquals("Metadata size should be ", 156, wav.metadata.totalSize)
        wav.totalAudioLength
        temp.delete()
    }
}
