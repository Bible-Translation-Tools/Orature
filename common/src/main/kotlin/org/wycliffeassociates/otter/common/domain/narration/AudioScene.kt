package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

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
        ActiveRecordingDrawable(incomingAudioStream, recordingActive, width / 2, secondsOnScreen / 2, recordingSampleRate)

    val frameBuffer = FloatArray(width * 2)

    var lastPositionRendered = -1

    fun getNarrationDrawable(location: Int): FloatArray {
        // if (lastPositionRendered != location) {
        // lastPositionRendered = location
        Arrays.fill(frameBuffer, 0f)
        val framesOnScreen = secondsOnScreen * recordingSampleRate

        val viewPortRange = getViewPortRange(location)
        val hasActiveData = activeDrawable.hasData()

        val readerData = readerDrawable.getWaveformDrawable(viewPortRange.first)
        System.arraycopy(readerData, 0, frameBuffer, 0, readerData.size)
        if (hasActiveData) {
            val readerEnd = existingAudioReader.totalFrames
            val activeData = activeDrawable.getWaveformDrawable()

            val paddedStart = if (readerEnd in viewPortRange) {
                framesToPixels(readerEnd - viewPortRange.first, width, framesOnScreen)
            } else 0

            if (readerEnd in viewPortRange) {
                val minMaxBufferStart = framesToPixels(readerEnd - viewPortRange.first, width, framesOnScreen) * 2
                logger.error("Starting active buffer read at $minMaxBufferStart")
                System.arraycopy(activeData, 0, frameBuffer, minMaxBufferStart, (frameBuffer.size / 2) - minMaxBufferStart)
            }
            else {
                System.arraycopy(activeData, 0, frameBuffer, 0, activeData.size)
//                val samplesBeyondReader = viewPortRange.last - readerEnd
//                if (samplesBeyondReader > 0) {
//                    val readerEndPosition = getReaderEndPosition(viewPortRange)
//                    System.arraycopy(activeData, 0, frameBuffer, min(readerEndPosition * 2, activeData.size), max((activeData.size - (readerEndPosition * 2)), 0))
//                }
            }
        }
        return frameBuffer
    }

    fun getReaderEndPosition(viewPortRange: IntRange): Int {
        val readerEndFrame = existingAudioReader.totalFrames
        val framesFromViewStart = viewPortRange.last - readerEndFrame
        return framesToPixels(framesFromViewStart, width,secondsOnScreen * recordingSampleRate)
    }

    fun getViewPortRange(location: Int): IntRange {
        val offset = (secondsOnScreen * recordingSampleRate) / 2
        return location - offset until location + offset
    }

    fun padStart(location: Int) {

    }

    private fun fillFromReader(location: Int): Int {
        existingAudioReader.seek(location)
        val readerPosition = existingAudioReader.framePosition

        val readerData = readerDrawable.getWaveformDrawable(location)
        val framesToFill = secondsOnScreen * recordingSampleRate

        val totalReaderFrames = existingAudioReader.totalFrames

        val framesFromReader = min(framesToFill, (totalReaderFrames - readerPosition))
        val pixelsFromReader = min(framesToPixels(framesFromReader, width, framesToFill) * 2, frameBuffer.size)

        System.arraycopy(readerData, 0, frameBuffer, 0, readerData.size)
        return pixelsFromReader
    }

    private fun fillFromActive(location: Int, pixelsToFill: Int) {
        if (pixelsToFill > 0) {
            val activeData = activeDrawable.getWaveformDrawable()
            System.arraycopy(activeData, 0, frameBuffer, frameBuffer.size - pixelsToFill, pixelsToFill)
        }
    }

    fun close() {
        activeDrawable.close()
    }
}