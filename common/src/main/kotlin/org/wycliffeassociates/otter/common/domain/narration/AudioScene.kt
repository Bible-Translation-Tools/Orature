package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.util.*
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
        ActiveRecordingDrawable(incomingAudioStream, recordingActive, width, secondsOnScreen, recordingSampleRate)

    val frameBuffer = FloatArray(width * 2)

    var lastPositionRendered = -1

    fun getNarrationDrawable(location: Int): FloatArray {
        // if (lastPositionRendered != location) {
            // lastPositionRendered = location
            Arrays.fill(frameBuffer, 0f)

            val read = fillFromReader(location)
            // fillFromActive(location, frameBuffer.size - read)
        //}
        return frameBuffer
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
}