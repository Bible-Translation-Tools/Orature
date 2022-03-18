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
package org.wycliffeassociates.otter.common.audio.mp3

import java.io.File
import java.io.IOException
import java.lang.Integer.max
import java.lang.Integer.min
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.AudioFormatStrategy
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.yellowcouch.javazoom.RandomAccessDecoder

// arbitrary size, though setting this too small results in choppy playback
private const val MP3_BUFFER_SIZE = 24576

internal class MP3FileReader(
    val file: File,
    start: Int? = null,
    end: Int? = null
) : AudioFormatStrategy, AudioFileReader {

    private var decoder: RandomAccessDecoder? = RandomAccessDecoder(file.absolutePath)

    val start = start ?: 0
    val end = end ?: decoder!!.sampleCount
    private var pos = min(max(0, this.start), this.end)

    override val sampleRate: Int = DEFAULT_SAMPLE_RATE
    override val channels: Int = DEFAULT_CHANNELS
    override val sampleSize: Int = DEFAULT_BITS_PER_SAMPLE
    override val framePosition: Int
        get() = pos - start

    override val totalFrames: Int
        get() = end - start
    override val bitsPerSample = DEFAULT_BITS_PER_SAMPLE

    override val metadata = Mp3Metadata(
        mp3File = file,
        cueFile = File(file.parent, "${file.nameWithoutExtension}.cue")
    )

    private val buff = ShortArray(MP3_BUFFER_SIZE * 2)

    override fun addCue(location: Int, label: String) {
        metadata.addCue(location, label)
    }

    override fun getCues(): List<AudioCue> {
        return metadata.getCues()
    }

    override fun update() {
        decoder?.stop()
        metadata.write()
    }

    private fun getPCMData(outBuff: ByteArray, pos: Int) {
        fillBuffers(pos, buff)
        val n = buff.size
        var j = 0
        for (i in 0 until min(n, outBuff.size) step 2) {
            val leftShort = buff[i].toInt()
            outBuff[j++] = (leftShort and 0xff).toByte()
            outBuff[j++] = (leftShort ushr 0x08 and 0xff).toByte()
        }
    }

    private fun fillBuffers(pos: Int, leftRight: ShortArray) {
        val sourceAudio = decoder!!.audioShorts
        var sourceIdx = 0
        try {
            sourceIdx = decoder!!.seek(pos, leftRight.size / 2) and RandomAccessDecoder.BUFFER_LAST
        } catch (e: IOException) {
            e.printStackTrace()
        }
        for (i in leftRight.indices) {
            leftRight[i] = sourceAudio[sourceIdx++]
            sourceIdx = sourceIdx and RandomAccessDecoder.BUFFER_LAST
        }
    }

    override fun hasRemaining(): Boolean {
        return pos < min(decoder!!.sampleCount, end)
    }

    override fun getPcmBuffer(bytes: ByteArray): Int {
        val remainingFrames = (end - pos)
        getPCMData(bytes, pos)
        pos += bytes.size / 2
        // remaining frames is multiplied by 2 for bitrate (16 bit)
        return bytes.size.coerceAtMost(remainingFrames * 2)
    }

    override fun seek(sample: Int) {
        // seek API should not be aware of audio outside of start and end;
        // so that a selected section can be treated as its own "track"
        var mappedSample = sample + start
        pos = max(start, min(mappedSample, end))
    }

    override fun open() {
    }

    override fun release() {
        decoder!!.stop()
        decoder = null
        pos = 0
        System.gc()
    }
}
