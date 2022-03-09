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

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.io.RandomAccessFile
import java.lang.IllegalStateException
import java.lang.Integer.max
import java.lang.Integer.min

internal class WavFileReader(
    val wav: WavFile,
    val playbackSectionBegin: Int? = null,
    val playbackSectionEnd: Int? = null
) : AudioFileReader {

    private val logger = LoggerFactory.getLogger(WavFileReader::class.java)

    override val totalFrames = wav.totalFrames
    override val sampleRate: Int = wav.sampleRate
    override val channels: Int = wav.channels
    override val sampleSize: Int = wav.bitsPerSample
    override val framePosition: Int
        get() = ((raf?.filePointer?.toInt() ?: 0) + start) / wav.frameSizeInBytes

    private var raf: RandomAccessFile? = null

    private var start = 0
    private var stop = 0

    override fun open() {
        val (begin, end) = computeBounds(wav)
        start = begin
        stop = end
        raf = RandomAccessFile(wav.file, "r")
    }

    fun computeBounds(wav: WavFile): Pair<Int, Int> {
        if (wav.file.length() <= WAV_HEADER_SIZE) {
            logger.info("Wav file ${wav.file.name} is just a header or empty, size is ${wav.file.length()}")
            return Pair(0, 0)
        }

        val totalFrames = wav.totalFrames
        var begin = if (playbackSectionBegin != null) min(max(0, playbackSectionBegin), totalFrames) else 0
        var end = if (playbackSectionEnd != null) min(max(begin, playbackSectionEnd), totalFrames) else totalFrames

        // Convert from frames to array index
        begin *= wav.frameSizeInBytes
        begin += WAV_HEADER_SIZE
        end *= wav.frameSizeInBytes
        end += WAV_HEADER_SIZE

        // Should be clamped between header size, computed beginning, and the file length
        val clampedBegin = max(WAV_HEADER_SIZE, min(begin, max(wav.file.length().toInt(), WAV_HEADER_SIZE)))
        val clampedEnd = max(clampedBegin, min(end, max(wav.file.length().toInt(), WAV_HEADER_SIZE)))

        if (clampedBegin != begin || clampedEnd != end) {
            logger.error("Error in file ${wav.file.name}")
            logger.error("Wanted to open for bounds: $begin to $end; file length is ${wav.file.length()}")
            logger.error("Bounds clamped to: $clampedBegin to $clampedEnd")
        }

        return Pair(clampedBegin, clampedEnd)
    }

    override fun getPcmBuffer(bytes: ByteArray): Int {
        raf?.let { raf ->
            val written = remaining().coerceAtMost(bytes.size)
            raf.read(bytes, 0, written)
            return written
        } ?: run {
            throw IllegalStateException("Tried to get pcm buffer before opening file")
        }
    }

    private fun remaining(): Int {
        return stop - ((raf?.filePointer?.toInt() ?: 0) + start)
    }

    @Throws(ArrayIndexOutOfBoundsException::class)
    override fun seek(sample: Int) {
        raf?.let { raf ->
            val index = min(wav.sampleIndex(sample) + start, stop)
            raf.seek(index.toLong())
        } ?: run {
            throw IllegalStateException("Tried to seek before opening file")
        }
    }

    override fun hasRemaining(): Boolean {
        if (raf != null) return remaining() > 0 else throw IllegalStateException("hasRemaining called before opening file")
    }

    override fun release() {
        raf?.let {
            it.close()
        }
        raf = null
    }
}
