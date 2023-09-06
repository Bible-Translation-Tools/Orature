package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.Observable
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import java.util.*
import kotlin.math.min

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

    fun hasData(): Boolean {
        return !activeRenderer.floatBuffer.isEmpty
    }
}