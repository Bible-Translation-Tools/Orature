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

import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by sarabiaj on 10/4/2016.
 */
class WavOutputStream @Throws(FileNotFoundException::class)
@JvmOverloads constructor(
    private val wav: WavFile,
    append: Boolean = false,
    private val buffered: Boolean = false
) : OutputStream(), Closeable, AutoCloseable {

    private val outputStream: OutputStream
    private lateinit var bos: BufferedOutputStream
    private var audioDataLength: Int = 0

    init {
        if (wav.file.length().toInt() == 0) {
            wav.initializeWavFile()
        }
        audioDataLength = wav.totalAudioLength
        // Truncate the metadata for writing
        // if appending, then truncate metadata following the audio length, otherwise truncate after the header
        val whereToTruncate = if (append) audioDataLength else 0
        try {
            FileOutputStream(wav.file, true)
                .channel
                .truncate((whereToTruncate + WAV_HEADER_SIZE).toLong())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // always need to use append to continue writing after the header rather than overwriting it
        outputStream = FileOutputStream(wav.file, true)
        if (buffered) {
            bos = BufferedOutputStream(outputStream)
        }
    }

    @Throws(IOException::class)
    override fun write(oneByte: Int) {
        if (buffered) {
            bos.write(oneByte)
        } else {
            outputStream.write(oneByte)
        }
        audioDataLength++
    }

    @Throws(IOException::class)
    override fun flush() {
        if (buffered) {
            bos.flush()
        }
        outputStream.flush()
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray) {
        if (buffered) {
            bos.write(bytes)
        } else {
            outputStream.write(bytes)
        }
        audioDataLength += bytes.size
    }

    @Throws(IOException::class)
    override fun close() {
        if (wav.hasMetadata) {
            val os = if (buffered) bos else outputStream
            wav.writeMetadata(os)
        }
        if (buffered) {
            bos.flush()
        }
        outputStream.flush()
        outputStream.close()
        wav.finishWrite(audioDataLength)
        updateHeader()
    }

    @Throws(IOException::class)
    internal fun updateHeader() {
        // file size minus riff size chunks
        val totalDataSize = wav.file.length() - CHUNK_HEADER_SIZE
        val bb = ByteBuffer.allocate(DWORD_SIZE)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        bb.putInt(totalDataSize.toInt())
        RandomAccessFile(wav.file, "rw").use { raf ->
            // move to total file size index
            raf.seek(DWORD_SIZE.toLong())
            raf.write(bb.array())
            bb.clear()
            bb.order(ByteOrder.LITTLE_ENDIAN)
            bb.putInt(audioDataLength)
            // move to audio size index
            raf.seek((WAV_HEADER_SIZE - CHUNK_LABEL_SIZE).toLong())
            raf.write(bb.array())
        }
    }
}
