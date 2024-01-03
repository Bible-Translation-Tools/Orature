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
package org.wycliffeassociates.otter.common.audio.wav

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

class WavHeaderTest {

    @Test
    fun `test normal wav without metadata chunks has 44 byte header`() {
        val testEnv = writeWav(0, 0, 0)
        val header = WavHeader().apply { parse(testEnv.file) }
        validateFormat(header)
        Assert.assertEquals("normal header should be 44 bytes", 44, header.totalHeaderSize)
        testEnv.file.delete()
    }

    @Test
    fun `test normal wav with metadata chunks has 44 byte header`() {
        val testEnv = writeWav(0, 0, 3)
        val header = WavHeader().apply { parse(testEnv.file) }
        validateFormat(header)
        Assert.assertEquals("normal header should be 44 bytes", 44, header.totalHeaderSize)
        testEnv.file.delete()
    }

    /**
     * This test validates that the audio is properly read when there are chunks prior to the fmt chunk, which means
     * that there is additional metadata prior to where the data section would typically being.
     *
     * We write STRT and END to a buffer of random size, and the beginning and end of the data chunk should start and
     * end with those values to signify all audio was properly accessed.
     *
     * We also validate that the format chunk is properly read.
     */
    @Test
    fun `test header has chunks before fmt`() {
        val testEnv = writeWav(5, 0, 3)
        val header = WavHeader().apply { parse(testEnv.file) }
        validateFormat(header)
        val reader = WavFileReader(WavFile(testEnv.file))
        reader.open()
        val audio = ByteArray(header.totalAudioLength)
        Assert.assertEquals("Audio size", testEnv.dataSize, header.totalAudioLength)
        reader.getPcmBuffer(audio)
        val buffer = ByteBuffer.wrap(audio)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        Assert.assertEquals("Audio data should begin with 'STRT', data size is ${testEnv.dataSize}", buffer.getText(4), "STRT")
        buffer.seek(audio.size - 8)
        Assert.assertEquals("Audio data should end with 'END ', data size is ${testEnv.dataSize}", buffer.getText(4), "END ")
        testEnv.file.delete()
    }

    /**
     * This test validates that the audio is properly read when there are chunks after the fmt chunk
     */
    @Test
    fun `test header has chunks after fmt`() {
        val testEnv = writeWav(0, 2, 3)
        val header = WavHeader().apply { parse(testEnv.file) }
        validateFormat(header)
        val reader = WavFileReader(WavFile(testEnv.file))
        reader.open()
        val audio = ByteArray(header.totalAudioLength)
        Assert.assertEquals("Audio size", testEnv.dataSize, header.totalAudioLength)
        reader.getPcmBuffer(audio)
        val buffer = ByteBuffer.wrap(audio)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        Assert.assertEquals("Audio data should begin with 'STRT', data size is ${testEnv.dataSize}", buffer.getText(4), "STRT")
        buffer.seek(audio.size - 8)
        Assert.assertEquals("Audio data should end with 'END ', data size is ${testEnv.dataSize}", buffer.getText(4), "END ")
        testEnv.file.delete()
    }

    /**
     * This test validates that the audio is properly read when there are chunks prior to and after the fmt chunk
     */
    @Test
    fun `test header has chunks before and after fmt`() {
        val testEnv = writeWav(5, 3, 3)
        val header = WavHeader().apply { parse(testEnv.file) }
        validateFormat(header)
        val reader = WavFileReader(WavFile(testEnv.file))
        reader.open()
        val audio = ByteArray(header.totalAudioLength)
        Assert.assertEquals("Audio size", testEnv.dataSize, header.totalAudioLength)
        reader.getPcmBuffer(audio)
        val buffer = ByteBuffer.wrap(audio)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        Assert.assertEquals("Audio data should begin with 'STRT', data size is ${testEnv.dataSize}", buffer.getText(4), "STRT")
        buffer.seek(audio.size - 8)
        Assert.assertEquals("Audio data should end with 'END ', data size is ${testEnv.dataSize}", buffer.getText(4), "END ")
        testEnv.file.delete()
    }

    private fun validateFormat(header: WavHeader) {
        Assert.assertEquals("bit rate should be 16", 16, header.bitsPerSample)
        Assert.assertEquals("channels should be 1", 1, header.channels)
        Assert.assertEquals("sample rate should be 44100", 44100, header.sampleRate)
    }
}

class AudioTestEnv(
    val file: File,
    val dataSize: Int
)

fun writeWav(
    preFmtChunks: Int,
    postFmtChunks: Int,
    postDataChunks: Int
): AudioTestEnv {
    val testFile = File.createTempFile("test", ".wav").apply { deleteOnExit() }

    val bytes = ByteBuffer.allocate(12)
    bytes.order(ByteOrder.LITTLE_ENDIAN)
    bytes.put("RIFF".toByteArray())
    bytes.putInt(0)
    bytes.put("WAVE".toByteArray())

    var dataSize = 0
    testFile.outputStream().use {
        it.write(bytes.array())
        for (i in 1..preFmtChunks) {
            writeArbitraryChunks(it, "JUNK")
        }
        writeFmtChunk(it)
        for (i in 1..postFmtChunks) {
            writeArbitraryChunks(it, "JUNK")
        }
        dataSize = writeArbitraryChunks(it, "data")
        for (i in 1..postDataChunks) {
            writeArbitraryChunks(it, "JUNK")
        }
    }

    return AudioTestEnv(testFile, dataSize)
}

fun writeArbitraryChunks(outputStream: OutputStream, label: String): Int {
    var dataSize = Random(System.currentTimeMillis()).nextInt(8, 500)
    if (label == "data" && dataSize % 2 == 1) { // data chunks of 16 bit audio must be even
        dataSize++
    }
    val padSize = if (dataSize % 2 == 0) 0 else 1
    val totalSize = dataSize + 8 + padSize
    val bytes = ByteBuffer.allocate(totalSize)
    bytes.order(ByteOrder.LITTLE_ENDIAN)
    bytes.put(label.toByteArray())
    bytes.putInt(dataSize)
    bytes.put("STRT".toByteArray())
    bytes.seek(totalSize - 4 - 12 - padSize)
    bytes.put("END ".toByteArray())

    outputStream.write(bytes.array())
    return dataSize
}

fun writeFmtChunk(outputStream: OutputStream) {
    val bytes = ByteBuffer.allocate(24)
    bytes.order(ByteOrder.LITTLE_ENDIAN)
    bytes.put("fmt ".toByteArray())
    bytes.putInt(16)
    bytes.putShort(1)
    bytes.putShort(1)
    bytes.putInt(44100)
    bytes.putInt(88200)
    bytes.putShort(2)
    bytes.putShort(16)
    outputStream.write(bytes.array())
}