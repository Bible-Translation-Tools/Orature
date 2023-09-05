package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.collections.FloatRingBuffer
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.PCMCompressor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class NarrationAudioScene(
    private val existingAudioReader: AudioFileReader,
    private val incomingAudioStream: Observable<ByteArray>,
    private val recordingActive: Observable<Boolean>,
    private val width: Int,
    private val secondsOnScreen: Int,
    private val recordingSampleRate: Int,
) {

    private val logger = LoggerFactory.getLogger(NarrationAudioScene::class.java)

    private val activeRenderer = ActiveRecordingRenderer(
        incomingAudioStream,
        recordingActive,
        width,
        secondsOnScreen
    )

    private val audioToRender = FloatArray(recordingSampleRate * secondsOnScreen)

    val compressedReaderData = FloatRingBuffer(width * 2)
    private val pcmCompressor = PCMCompressor(compressedReaderData, samplesToCompress(width, secondsOnScreen))

    val frameBuffer = FloatArray(width * 2)

    fun getFrameData(): FloatArray {
        try {
            val activeData = activeRenderer.floatBuffer
            val activeSize = activeData.size()

            Arrays.fill(audioToRender, 0f)
            val activeStartPos = frameBuffer.size - activeSize

            val sizeFromReader = recordingSampleRate * secondsOnScreen * 2

            // If there is active recording data, and we should still be showing data from the reader
            // if (activeStartPos > 0 && activeStartPos != audioToRender.size) {
            // positionReader(existingAudioReader, sizeFromReader)
            // } else {
            // existingAudioReader.seek(readPosition)
            // }

            val read = getDataFromReader(existingAudioReader, audioToRender, sizeFromReader)
            compressReadData(pcmCompressor, audioToRender, read)

            val readerDataSize = compressedReaderData.size()

            val adjustedStart = min(readerDataSize, activeStartPos)
            val adjustedEnd = min(frameBuffer.size, activeSize)

//            logger.error("adjusted start is $adjustedStart")
//            logger.error("adjustedEnd is ${adjustedEnd}")

            for (i in 0 until adjustedStart) {
                frameBuffer[i] = compressedReaderData[i]
            }

            for (i in 0 until adjustedEnd - adjustedStart) {
                frameBuffer[adjustedStart + i] = activeData[i]
            }

            compressedReaderData.clear()

            return frameBuffer
        } catch (e: Exception) {
            logger.error(e.message)
        }
        throw IllegalStateException()
    }

    private fun compressReadData(
        pcmCompressor: PCMCompressor,
        frameBuffer: FloatArray,
        read: Int
    ) {
        for (i in 0 until min(read, frameBuffer.size)) {
            pcmCompressor.add(frameBuffer[i])
        }
    }

    private fun positionReader(reader: AudioFileReader, framesFromEnd: Int) {
        reader.seek(reader.totalFrames - framesFromEnd)
    }

    private fun getDataFromReader(
        reader: AudioFileReader,
        outBuff: FloatArray,
        sizeToRead: Int
    ): Int {
        val framesRead = ByteArray(sizeToRead * 2)

        val read = reader.getPcmBuffer(framesRead)
        val bb = ByteBuffer.wrap(framesRead)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        for (i in outBuff.indices) {
            outBuff[i] = bb.getShort().toFloat()
        }
        for (i in read until outBuff.size) {
            outBuff[i] = 0f
        }
        return read
    }

    private fun getDataFromActive(activeData: FloatArray, outBuff: FloatArray, startPos: Int) {
        // System.arraycopy(activeData, 0, outBuff, startPos, outBuff.size - startPos)
    }

    /** Clears rendered data from buffer */
    fun resetRecordingRenderer() {
        activeRenderer.clearData()
    }

    private fun samplesToCompress(width: Int, secondsOnScreen: Int): Int {
        return (existingAudioReader.sampleRate * secondsOnScreen) / width
    }
}

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

        val clampedLoc = location.coerceIn(0..audioReader.totalFrames)
        audioReader.seek(clampedLoc)

        val totalSamplesToRead = secondsOnScreen * recordingSampleRate

//        var paddedFrames = 0
//        if (location < 0 && padEnds > 0) {
//            paddedFrames = padStart(
//                pcmCompressor,
//                audioReader.sampleSize / 8,
//                floor(padEnds * totalSamplesToRead).toInt(),
//                location
//            )
//        }

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

    private fun padStart(pcmCompressor: PCMCompressor, frameSize: Int, framesToPad: Int, location: Int): Int {
        val count = max(framesToPad - (location.absoluteValue * frameSize), 0)
        for (i in 0 until count) {
            pcmCompressor.add(0f)
        }
        return count
    }
}

class ActiveRecordingDrawable(
    private val incomingAudioStream: Observable<ByteArray>,
    private val recordingActive: Observable<Boolean>,
    private val width: Int,
    private val secondsOnScreen: Int,
    private val recordingSampleRate: Int,
) {

    private val activeRenderer = ActiveRecordingRenderer(
        incomingAudioStream,
        recordingActive,
        width,
        secondsOnScreen
    )

    private val waveformDrawable = FloatArray(width * 2)

    fun getWaveformDrawable(): FloatArray {
        Arrays.fill(waveformDrawable, 0f)

        val activeData = activeRenderer.floatBuffer
        val activeSize = activeData.size()

        val totalSamplesToRead = secondsOnScreen * recordingSampleRate

        val samplesFromActive = min(totalSamplesToRead, activeSize)
        for (i in 0 until samplesFromActive) {
            waveformDrawable[i] = activeData[i]
        }

        return waveformDrawable
    }

    private fun samplesToCompress(width: Int, secondsOnScreen: Int): Int {
        return (recordingSampleRate * secondsOnScreen) / width
    }

    fun clearBuffer() {
        activeRenderer.clearData()
    }
}

class AudioScene(
    private val existingAudioReader: AudioFileReader,
    private val incomingAudioStream: Observable<ByteArray>,
    private val recordingActive: Observable<Boolean>,
    private val width: Int,
    private val secondsOnScreen: Int,
    private val recordingSampleRate: Int,
) {

    private val logger = LoggerFactory.getLogger(AudioScene::class.java)

    val readerDrawable = AudioReaderDrawable(existingAudioReader, width, secondsOnScreen, recordingSampleRate)
    val activeDrawable =
        ActiveRecordingDrawable(incomingAudioStream, recordingActive, width, secondsOnScreen, recordingSampleRate)

    val frameBuffer = FloatArray(width * 2)

    var lastPositionRendered = -1

    fun getNarrationDrawable(location: Int): FloatArray {
        // if (lastPositionRendered != location) {
            lastPositionRendered = location
            Arrays.fill(frameBuffer, 0f)
            val read = fillFromReader(location)
            fillFromActive(location, frameBuffer.size - read)
        //}
        return frameBuffer
    }

    private fun fillFromReader(location: Int): Int {
        existingAudioReader.seek(location)
        val readerPosition = existingAudioReader.framePosition

        val readerData = readerDrawable.getWaveformDrawable(location)
        val framesToFill = secondsOnScreen * recordingSampleRate

        val totalReaderFrames = existingAudioReader.totalFrames

        val framesFromReader = min(framesToFill, (totalReaderFrames - readerPosition))
        val pixelsFromReader = min(framesToPixels(framesFromReader, width, framesToFill) * 2, frameBuffer.size)

        System.arraycopy(readerData, 0, frameBuffer, 0, pixelsFromReader)
        return pixelsFromReader
    }

    private fun fillFromActive(location: Int, pixelsToFill: Int) {
        if (pixelsToFill > 0) {
            val activeData = activeDrawable.getWaveformDrawable()
            System.arraycopy(activeData, 0, frameBuffer, frameBuffer.size - pixelsToFill, pixelsToFill)
        }
    }
}

fun framesToPixels(frames: Int, width: Int, framesOnScreen: Int): Int {
    val framesInPixel = framesOnScreen / width.toFloat()
    return (frames / framesInPixel).toInt()
}