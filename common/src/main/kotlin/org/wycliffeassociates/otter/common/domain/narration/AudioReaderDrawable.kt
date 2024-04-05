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
package org.wycliffeassociates.otter.common.domain.narration

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.collections.FloatRingBuffer
import org.wycliffeassociates.otter.common.recorder.PCMCompressor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.max
import kotlin.math.min

class AudioReaderDrawable(
    private val audioReader: AudioFileReader,
    private val width: Int,
    private val secondsOnScreen: Int,
    private val recordingSampleRate: Int,
) {
    private val logger = LoggerFactory.getLogger(AudioReaderDrawable::class.java)

    // Buffer sizes for drawables doubled, since each pixel needs a minimum and maximum value to draw a line
    private val waveformDrawable = FloatArray(width * 2)
    private val drawableData = FloatRingBuffer(width * 2)
    private val pcmCompressor = PCMCompressor(drawableData, framesToCompress(width, secondsOnScreen))

    private val tempBuff = ByteArray(DEFAULT_BUFFER_SIZE)
    private val bb = ByteBuffer.wrap(tempBuff).apply { order(ByteOrder.LITTLE_ENDIAN) }

    private fun framesToCompress(width: Int, secondsOnScreen: Int): Int {
        return (recordingSampleRate * secondsOnScreen) / width
    }

    /**
     * Given a particular location, compute a buffer of min max values to draw a waveform from.
     *
     * If the location is negative, it will be zero padded.
     *
     * @param location the location in frames to begin reading from
     * @return an array of min/max values to draw
     */
    fun getWaveformDrawable(location: Int): FloatArray {
        Arrays.fill(waveformDrawable, 0f)
        drawableData.clear()
        pcmCompressor.clear()

        var totalFramesToRead = secondsOnScreen * recordingSampleRate

        val paddedFrames = padStart(pcmCompressor, audioReader.frameSizeBytes, location, totalFramesToRead)

        pcmCompressor.clear() // clear the compressor after potentially padding 0s
        totalFramesToRead -= paddedFrames

        val clampedFrameLoc = location.coerceIn(0..audioReader.totalFrames)
        audioReader.seek(clampedFrameLoc)

        val frameSizeBytes = audioReader.frameSizeBytes
        var framesToRead = max(min(totalFramesToRead, audioReader.totalFrames - clampedFrameLoc), 0)

        var retry = 0
        while (framesToRead > 0 && audioReader.hasRemaining()) {
            if (retry >= 10) {
                logger.error("Aborted reader renderer, read returned 0 bytes several times")
                break
            }

            val bytesRead = audioReader.getPcmBuffer(tempBuff)
            val framesRead = bytesRead / frameSizeBytes

            // Read could return 0 if the underlying reader needs to hop over to the next verse; but if this happens
            // many times in a row, we're in an infinite loop.
            if (bytesRead == 0) retry++ else retry = 0

            val bytesToTransfer = if (framesToRead > framesRead) bytesRead else framesToRead * frameSizeBytes
            transferBytesToCompressor(pcmCompressor, bb, bytesToTransfer)
            framesToRead -= framesRead
        }

        for (i in waveformDrawable.indices) {
            waveformDrawable[i] = drawableData[i]
        }

        return waveformDrawable
    }

    private fun transferBytesToCompressor(
        pcmCompressor: PCMCompressor,
        byteBuffer: ByteBuffer,
        bytesToRead: Int
    ) {
        byteBuffer.position(0)
        for (i in 0 until bytesToRead / 2) {
            // FIXME: Shorts only work for a bit rate of 16
            pcmCompressor.add(bb.getShort().toFloat())
        }
    }

    /**
     * If the location was negative (the renderer is trying to move the starting position), then push 0's to the
     * compressor until the read head is 0
     *
     * @return returns the number of frames read
     */
    private fun padStart(pcmCompressor: PCMCompressor, frameSize: Int, frameLocation: Int, framesToRead: Int): Int {
        if (frameLocation >= 0) { return 0 }

        var framesRead = 0
        for (i in frameLocation until 0) {
            if (framesRead >= framesToRead) return framesRead
            pcmCompressor.add(0f)
            framesRead++
        }
        return framesRead
    }
}