package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.scene.control.Skin
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.WaveformSliderSkin

class AudioSlider(
    val file: WavFile,
    min: Double = 0.0,
    max: Double = file.totalFrames.toDouble(),
    value: Double = 0.0
) : Slider(min, max, value) {
    override fun createDefaultSkin(): Skin<*> {
        return WaveformSliderSkin(this)
    }
}
