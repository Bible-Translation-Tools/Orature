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
    private val readerDrawable: AudioReaderDrawable = AudioReaderDrawable(
        existingAudioReader,
        width,
        secondsOnScreen,
        recordingSampleRate
    ),
    private val activeDrawable: ActiveRecordingDrawable = ActiveRecordingDrawable(
        incomingAudioStream,
        recordingActive,
        width / 2,
        secondsOnScreen / 2,
        recordingSampleRate
    )
) {
    private val logger = LoggerFactory.getLogger(AudioScene::class.java)

    val frameBuffer = FloatArray(width * 2)

    fun getNarrationDrawable(location: Int): Pair<FloatArray, IntRange> {
        Arrays.fill(frameBuffer, 0f)
        val framesOnScreen = secondsOnScreen * recordingSampleRate

        val viewPortRange = getViewPortRange(location)

        val hasActiveData = activeDrawable.hasData()
        val readerData = readerDrawable.getWaveformDrawable(viewPortRange.first)

        // Copy reader data
        System.arraycopy(readerData, 0, frameBuffer, 0, readerData.size)

        // If there is active data, we can apply it by overwriting the read data
        if (hasActiveData) {
            val readerEnd = existingAudioReader.totalFrames
            val activeData = activeDrawable.getWaveformDrawable()

            if (readerEnd in viewPortRange) {
                // multiply by two because each pixel is a min and a max
                val minMaxBufferStart = framesToPixels(readerEnd - viewPortRange.first, width, framesOnScreen) * 2
                System.arraycopy(
                    activeData,
                    // due to how the ring buffer works, the oldest recorded data will begin at 0
                    0,
                    frameBuffer,
                    minMaxBufferStart,
                    (frameBuffer.size / 2) - minMaxBufferStart)
            }
            else {
                System.arraycopy(activeData, 0, frameBuffer, 0, activeData.size)
            }
        }
        return Pair(frameBuffer, viewPortRange)
    }

    /**
     * Given an audio frame, compute the range of audio frames that will be displayed by this scene
     * Currently this is hardcoded to be from half the seconds on screen prior to the location, and half after.
     */
    fun getViewPortRange(location: Int): IntRange {
        val offset = (secondsOnScreen * recordingSampleRate) / 2
        return location - offset until location + offset
    }

    fun clear() {
        activeDrawable.clearBuffer()
    }

    fun close() {
        activeDrawable.close()
    }
}

fun framesToPixels(frames: Int, width: Int, framesOnScreen: Int): Int {
    val framesInPixel = framesOnScreen / width.toFloat()
    return (frames / framesInPixel).toInt()
}