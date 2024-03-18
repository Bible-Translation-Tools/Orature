/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import kotlin.math.max

interface IWaveformViewModel {
    var sampleRate: Int
    var totalFrames: Int
    val totalFramesProperty: IntegerProperty
    val waveformAudioPlayerProperty: ObjectProperty<IAudioPlayer>
    val positionProperty: DoubleProperty
    val imageWidthProperty: DoubleProperty
    val audioPositionProperty: IntegerProperty

    fun pixelsInHighlight(controlWidth: Double): Double {
        if (sampleRate == 0 || totalFrames == 0) {
            return 1.0
        }

        val framesInHighlight = sampleRate * SECONDS_ON_SCREEN
        val framesPerPixel = totalFrames / max(controlWidth, 1.0)
        return max(framesInHighlight / framesPerPixel, 1.0)
    }

    fun getLocationInFrames(): Int {
        return waveformAudioPlayerProperty.value?.getLocationInFrames() ?: 0
    }

    fun getDurationInFrames(): Int {
        return waveformAudioPlayerProperty.value?.getDurationInFrames() ?: 0
    }

    fun computeImageWidth(width: Int, secondsOnScreen: Int = SECONDS_ON_SCREEN): Double {
        val samplesPerScreenWidth = sampleRate * secondsOnScreen
        val samplesPerPixel = samplesPerScreenWidth / width
        return waveformAudioPlayerProperty.value.getDurationInFrames() / samplesPerPixel.toDouble()
    }

    fun calculatePosition() {
        waveformAudioPlayerProperty.value?.let { audioPlayer ->
            val current = audioPlayer.getLocationInFrames()
            val duration = audioPlayer.getDurationInFrames().toDouble()
            val percentPlayed = current / duration
            val pos = percentPlayed * imageWidthProperty.value
            positionProperty.set(pos)
            audioPositionProperty.set(current)
        }
    }
}