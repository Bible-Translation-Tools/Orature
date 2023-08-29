package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.min

class NarrationAudioRenderer(
    val existingAudioReader: AudioFileReader,
    val incomingAudioStream: Observable<ByteArray>,
    recordingActive: Observable<Boolean>,
    val width: Int,
    secondsOnScreen: Int,
    private val recordingSampleRate: Int,
) {
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
        val activeStartPos = frameBuffer.size - activeSize

        val sizeFromReader = min((activeStartPos / 2) - 1, 0)

        if (activeStartPos != frameBuffer.size) {
            positionReader(existingAudioReader, sizeFromReader)
        }

        getDataFromReader(existingAudioReader, frameBuffer, sizeFromReader)
        getDataFromActive(activeData.array, frameBuffer, activeStartPos)

        return frameBuffer
    }

    private fun positionReader(reader: AudioFileReader, framesFromEnd: Int) {
        reader.seek(reader.totalFrames - framesFromEnd)
    }

    private fun getDataFromReader(reader: AudioFileReader, outBuff: FloatArray, sizeToRead: Int) {
        val framesRead = ByteArray(sizeToRead * reader.sampleSize)
        reader.getPcmBuffer(framesRead)
        val bb = ByteBuffer.wrap(framesRead)
        System.arraycopy(bb.asFloatBuffer().array(), 0, outBuff, 0, sizeToRead)
    }

    private fun getDataFromActive(activeData: FloatArray, outBuff: FloatArray, startPos: Int) {
        System.arraycopy(activeData, 0, outBuff, startPos, outBuff.size - startPos)
    }

    /** Clears rendered data from buffer */
    fun resetRecordingRenderer() {
        activeRenderer.clearData()
    }
}