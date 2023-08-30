package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.max
import kotlin.math.min

class NarrationAudioScene(
    val existingAudioReader: AudioFileReader,
    val incomingAudioStream: Observable<ByteArray>,
    recordingActive: Observable<Boolean>,
    val width: Int,
    secondsOnScreen: Int,
    private val recordingSampleRate: Int,
) {

    private val logger = LoggerFactory.getLogger(NarrationAudioScene::class.java)

    private val activeRenderer = ActiveRecordingRenderer(
        incomingAudioStream,
        recordingActive,
        width,
        secondsOnScreen
    )

    private val frameBuffer = FloatArray(recordingSampleRate * secondsOnScreen * 2)

    fun getFrameData(): FloatArray {
        val activeData = activeRenderer.floatBuffer
        val activeSize = activeData.size()

        Arrays.fill(frameBuffer, 0f)
        val activeStartPos = activeSize

        val sizeFromReader = max((frameBuffer.size - activeStartPos / 2) - 1, 0)

        logger.info("Active start position is $activeStartPos, should only read $sizeFromReader from reader out of ${frameBuffer.size / 2}")

        if (activeStartPos != frameBuffer.size) {
            positionReader(existingAudioReader, sizeFromReader)
        }

        getDataFromReader(existingAudioReader, frameBuffer, sizeFromReader)
        if (activeStartPos < frameBuffer.size) {
            getDataFromActive(activeData.array, frameBuffer, activeStartPos)
        }

        return frameBuffer
    }

    private fun positionReader(reader: AudioFileReader, framesFromEnd: Int) {
        reader.seek(reader.totalFrames - framesFromEnd)
    }

    private fun getDataFromReader(reader: AudioFileReader, outBuff: FloatArray, sizeToRead: Int) {
        val framesRead = ByteArray(sizeToRead * reader.sampleSize)
        val read = reader.getPcmBuffer(framesRead)
        val bb = ByteBuffer.wrap(framesRead)
        val fb = bb.asFloatBuffer()
        for (i in 0 until read) {
            outBuff[i] = fb.get()
        }
        for (i in read until outBuff.size) {
            outBuff[i] = 0f
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
}