package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.animation.AnimationTimer
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.model.SECONDS_ON_SCREEN
import kotlin.math.max

interface IWaveformViewModel {
    var sampleRate: Int
    var totalFrames: Int
    val waveformAudioPlayerProperty: ObjectProperty<IAudioPlayer>
    val positionProperty: SimpleDoubleProperty
    var imageWidthProperty: SimpleDoubleProperty
    var timer: AnimationTimer?

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
        val pixelsInDuration = waveformAudioPlayerProperty.value.getDurationInFrames() / samplesPerPixel
        return pixelsInDuration.toDouble()
    }

    fun startAnimationTimer() {
        timer = object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                calculatePosition()
            }
        }.apply { start() }
    }

    fun stopAnimationTimer() {
        timer?.stop()
        timer = null
    }

    fun calculatePosition() {
        waveformAudioPlayerProperty.value?.let { audioPlayer ->
            val current = audioPlayer.getLocationInFrames()
            val duration = audioPlayer.getDurationInFrames().toDouble()
            val percentPlayed = current / duration
            val pos = percentPlayed * imageWidthProperty.value
            positionProperty.set(pos)
        }
    }
}