package org.wycliffeassociates.otter.common.audio.wav

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavFileReaderTest {

    @Test
    fun `test that mapped buffer is released`() {
        val file = File.createTempFile("file", ".wav")
        file.deleteOnExit()
        val wavFile = WavFile(file, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
        val wos = WavOutputStream(wavFile)
        wos.use { for (i in 0 until 100) it.write(i) }
        val reader = WavFileReader(wavFile).also { it.open() }
        val buffer = ByteArray(100)
        reader.getPcmBuffer(buffer)
        val bb = ByteBuffer.wrap(buffer)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val shorts = bb
        var i = 0
        while (shorts.hasRemaining()) {
            assertEquals("", i.toByte(), shorts.get())
            i++
        }
        reader.release()
        assertTrue(file.delete())
    }
}
