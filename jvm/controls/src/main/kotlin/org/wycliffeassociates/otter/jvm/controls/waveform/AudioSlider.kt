package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Skin
import javafx.scene.control.Slider
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.WaveformSliderSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow

class AudioSlider(
    min: Double = 0.0,
    max: Double = 1.0,
    value: Double = 0.0
) : Slider(min, max, value) {

    val thumbFillProperty = SimpleObjectProperty<Paint>(Paint.valueOf("#00000015"))
    val thumbLineColorProperty = SimpleObjectProperty<Paint>(Color.BLACK)
    val secondsToHighlightProperty = SimpleIntegerProperty(1)

    val player = SimpleObjectProperty<IAudioPlayer>()

    init {
        player.onChangeAndDoNow { player ->
            player?.let {
                setMax(it.getAbsoluteDurationInFrames().toDouble())
                it.seek(0)
            }
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return WaveformSliderSkin(this)
    }
}
