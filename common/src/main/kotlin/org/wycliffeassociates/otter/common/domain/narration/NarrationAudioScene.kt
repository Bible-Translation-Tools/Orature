package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.collections.FloatRingBuffer
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.PCMCompressor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.max

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

    val floatBuffer = FloatRingBuffer(width * 2)
    private val pcmCompressor = PCMCompressor(floatBuffer, samplesToCompress(width, secondsOnScreen))

    val frameBuffer = FloatArray(width * 2)

    fun getFrameData(readPosition: Int = 0): FloatArray {
        try {
            val activeData = activeRenderer.floatBuffer
            val activeSize = activeData.size()

            Arrays.fill(audioToRender, 0f)
            val activeStartPos = frameBuffer.size - activeSize

            val sizeFromReader = recordingSampleRate * secondsOnScreen * 2

            logger.info("Active start position is $activeStartPos, should only read $sizeFromReader from reader out of ${audioToRender.size}")

            // If there is active recording data, and we should still be showing data from the reader
            // if (activeStartPos > 0 && activeStartPos != audioToRender.size) {
            // positionReader(existingAudioReader, sizeFromReader)
            // } else {
            existingAudioReader.seek(readPosition)
            // }

            getDataFromReader(existingAudioReader, audioToRender, sizeFromReader)
            compressReadData(pcmCompressor, audioToRender)

            for (i in 0 until activeStartPos) {
                frameBuffer[i] = floatBuffer[i]
            }
            for (i in activeStartPos until frameBuffer.size) {
                frameBuffer[i] = activeData[i]
            }

            return frameBuffer
        } catch (e: Exception) {
            logger.error(e.message)
        }
        throw IllegalStateException()
    }

    private fun compressReadData(
        pcmCompressor: PCMCompressor,
        frameBuffer: FloatArray,
    ) {
        for (element in frameBuffer) {
            pcmCompressor.add(element)
        }
    }

    private fun positionReader(reader: AudioFileReader, framesFromEnd: Int) {
        reader.seek(reader.totalFrames - framesFromEnd)
    }

    private fun getDataFromReader(reader: AudioFileReader, outBuff: FloatArray, sizeToRead: Int) {
        try {
            val framesRead = ByteArray(sizeToRead * 2)

            val read = reader.getPcmBuffer(framesRead)
            val bb = ByteBuffer.wrap(framesRead)
            bb.order(ByteOrder.LITTLE_ENDIAN)
            for (i in 0 until outBuff.size) {
                outBuff[i] = bb.getShort().toFloat()
            }
            for (i in read until outBuff.size) {
                outBuff[i] = 0f
            }
        } catch (e: Exception) {
            logger.error(e.message)
        }
        // logger.info(outBuff.joinToString { "$it," })
        // System.arraycopy(bb.asFloatBuffer().asReadOnlyBuffer().array(), 0, outBuff, 0, sizeToRead)
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