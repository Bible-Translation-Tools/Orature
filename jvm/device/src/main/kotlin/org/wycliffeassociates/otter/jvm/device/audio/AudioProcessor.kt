/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.device.audio

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter
import be.tarsos.dsp.io.TarsosDSPAudioFormat

class AudioProcessor {

    val monitor = Object()

    val processorFormat = TarsosDSPAudioFormat(44100f, 16, 1, true, false)
    val event = AudioEvent(processorFormat)
    var playbackRate = 1.0

    var wsola = WaveformSimilarityBasedOverlapAdd(
        WaveformSimilarityBasedOverlapAdd.Parameters.speechDefaults(
            playbackRate,
            processorFormat.sampleRate.toDouble()
        )
    )

    val overlap: Int
        get() = synchronized(monitor) { wsola.overlap }

    val inputBufferSize: Int
        get() = synchronized(monitor) { wsola.inputBufferSize }

    fun updatePlaybackRate(rate: Double) {
        playbackRate = rate
        synchronized(monitor) {
            wsola = WaveformSimilarityBasedOverlapAdd(
                WaveformSimilarityBasedOverlapAdd.Parameters.speechDefaults(
                    playbackRate,
                    processorFormat.sampleRate.toDouble()
                )
            )
        }
    }

    fun process(bytes: ByteArray): ByteArray {
        val floats = FloatArray(bytes.size / 2)
        TarsosDSPAudioFloatConverter.getConverter(processorFormat).toFloatArray(
            bytes,
            floats
        )
        event.floatBuffer = floats
        synchronized(monitor) {
            wsola.process(event)
        }
        return event.byteBuffer
    }
}
