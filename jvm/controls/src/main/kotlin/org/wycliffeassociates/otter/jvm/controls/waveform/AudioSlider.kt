package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Skin
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.WaveformSliderSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow

class AudioSlider(
    min: Double = 0.0,
    max: Double = 1.0,
    value: Double = 0.0
) : Slider(min, max, value) {

    val player = SimpleObjectProperty<IAudioPlayer>()

    init {
        player.onChangeAndDoNow { player ->
            player?.let {
                setMax(it.getAbsoluteDurationInFrames().toDouble())
            }
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return WaveformSliderSkin(this)
    }
}
