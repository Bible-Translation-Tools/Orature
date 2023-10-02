package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.ObjectProperty
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import kotlin.math.max

interface IWaveformViewModel {
    var sampleRate: Int
    var targetTotalFrames: Int
    val targetPlayerProperty: ObjectProperty<IAudioPlayer>

    fun pixelsInHighlight(controlWidth: Double): Double {
        if (sampleRate == 0 || targetTotalFrames == 0) {
            return 1.0
        }

        val framesInHighlight = sampleRate * SECONDS_ON_SCREEN
        val framesPerPixel = targetTotalFrames / max(controlWidth, 1.0)
        return max(framesInHighlight / framesPerPixel, 1.0)
    }

    fun getLocationInFrames(): Int {
        return targetPlayerProperty.value?.getLocationInFrames() ?: 0
    }

    fun getDurationInFrames(): Int {
        return targetPlayerProperty.value?.getDurationInFrames() ?: 0
    }
}