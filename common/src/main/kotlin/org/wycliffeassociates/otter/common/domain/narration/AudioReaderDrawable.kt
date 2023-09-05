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
    // private val padEnds: Float = 0f
) {
    private val logger = LoggerFactory.getLogger(AudioReaderDrawable::class.java)

    private val waveformDrawable = FloatArray(width * 2)
    private val drawableData = FloatRingBuffer(width * 2)
    private val pcmCompressor = PCMCompressor(drawableData, samplesToCompress(width, secondsOnScreen))

    private val tempBuff = ByteArray(DEFAULT_BUFFER_SIZE)
    private val bb = ByteBuffer.wrap(tempBuff).apply { order(ByteOrder.LITTLE_ENDIAN) }

    private fun samplesToCompress(width: Int, secondsOnScreen: Int): Int {
        return (recordingSampleRate * secondsOnScreen) / width
    }

    fun getWaveformDrawable(location: Int): FloatArray {
        Arrays.fill(waveformDrawable, 0f)
        drawableData.clear()
        var totalSamplesToRead = secondsOnScreen * recordingSampleRate

        val paddedLoc = location - 221000

        val paddedFrames = padStart(pcmCompressor, audioReader.sampleSize / 8, paddedLoc, totalSamplesToRead)
        totalSamplesToRead -= paddedFrames

        val clampedLoc = paddedLoc.coerceIn(0..audioReader.totalFrames)
        audioReader.seek(clampedLoc)

        val frameSizeBytes = audioReader.sampleSize / 8


        var framesToRead = max(min(totalSamplesToRead, audioReader.totalFrames - clampedLoc), 0) * frameSizeBytes


        while (framesToRead > 0) {
            val read = audioReader.getPcmBuffer(tempBuff)
            val toTransfer = if (framesToRead > read) read else framesToRead
            transferBytesToCompressor(pcmCompressor, bb, toTransfer, frameSizeBytes)
            framesToRead -= read
        }

        for (i in waveformDrawable.indices) {
            waveformDrawable[i] = drawableData[i]
        }

        return waveformDrawable
    }

    private fun transferBytesToCompressor(
        pcmCompressor: PCMCompressor,
        byteBuffer: ByteBuffer,
        framesToRead: Int,
        frameSizeBytes: Int
    ) {
        byteBuffer.position(0)
        for (i in 0 until framesToRead / frameSizeBytes) {
            pcmCompressor.add(bb.getShort().toFloat())
        }
    }

    /**
     * If the location was negative (the renderer is trying to move the starting position), then push 0's to the
     * compressor until the read head is 0
     *
     * @return returns the number of frames read
     */
    private fun padStart(pcmCompressor: PCMCompressor, frameSize: Int, location: Int, framesToRead: Int): Int {
        if (location >= 0) { return 0 }

        var framesRead = 0
        for (i in location until 0) {
            if (framesRead >= framesToRead) return framesRead
            pcmCompressor.add(0f)
            framesRead++
        }
        return framesRead
    }
}