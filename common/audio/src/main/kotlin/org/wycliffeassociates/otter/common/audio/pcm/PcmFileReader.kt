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
package org.wycliffeassociates.otter.common.audio.pcm

import org.wycliffeassociates.otter.common.audio.*
import java.io.RandomAccessFile
import java.lang.Exception
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

internal class PcmFileReader(
    val pcm: PcmFile,
    val start: Int? = null,
    val end: Int? = null
) : AudioFileReader {
    override val sampleRate: Int = pcm.sampleRate
    override val channels: Int = pcm.channels
    override val sampleSize: Int = pcm.bitsPerSample
    override val framePosition: Int
        get() = (mappedFile?.position() ?: 0) / pcm.frameSizeInBytes
    override val totalFrames: Int = pcm.totalFrames

    private var mappedFile: MappedByteBuffer? = null
    private var channel: FileChannel? = null

    override fun hasRemaining(): Boolean {
        return mappedFile?.hasRemaining() ?: throw IllegalStateException("hasRemaining called before opening file")
    }

    override fun getPcmBuffer(bytes: ByteArray): Int {
        mappedFile?.let { _mappedFile ->
            val written = _mappedFile.remaining().coerceAtMost(bytes.size)
            _mappedFile.get(bytes, 0, written)
            return written
        } ?: run {
            throw IllegalStateException("Tried to get pcm buffer before opening file")
        }
    }

    @Throws(ArrayIndexOutOfBoundsException::class)
    override fun seek(sample: Int) {
        mappedFile?.let { _mappedFile ->
            val index = Integer.min(pcm.sampleIndex(sample), _mappedFile.limit())
            _mappedFile.position(index)
        } ?: run {
            throw IllegalStateException("Tried to seek before opening file")
        }
    }

    override fun open() {
        mappedFile?.let { release() }
        val (begin, end) = computeBounds()
        mappedFile =
            RandomAccessFile(pcm.file, "r").use {
                channel = it.channel
                channel!!.map(
                    FileChannel.MapMode.READ_ONLY,
                    begin.toLong(),
                    (end - begin).toLong()
                )
            }
    }

    override fun release() {
        if (mappedFile != null) {
            try {
                // https://stackoverflow.com/questions/25238110/how-to-properly-close-mappedbytebuffer/25239834#25239834
                // TODO: Replace with https://docs.oracle.com/en/java/javase/14/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html#ofByteBuffer(java.nio.ByteBuffer)
                val unsafeClass = Class.forName("sun.misc.Unsafe")
                val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
                unsafeField.isAccessible = true
                val unsafe: Any = unsafeField.get(null)
                val invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer::class.java)
                invokeCleaner.invoke(unsafe, mappedFile)
            } catch (e: Exception) {

            }
            channel?.close()
            mappedFile = null
            channel = null
            System.gc()
        }
    }

    private fun computeBounds(): Pair<Int, Int> {
        val fileLength = pcm.file.length().toInt()
        val begin = if (start != null) Integer.min(Integer.max(0, start), fileLength) else 0
        val end = if (end != null) Integer.min(Integer.max(begin, end), fileLength) else fileLength

        return Pair(begin, end)
    }

}
