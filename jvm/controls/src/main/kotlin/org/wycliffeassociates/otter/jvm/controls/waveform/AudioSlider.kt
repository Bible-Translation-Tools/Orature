/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.scene.control.Skin
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.WaveformSliderSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow

class AudioSlider(
    min: Double = 0.0,
    max: Double = 1.0,
    value: Double = 0.0
) : Slider(min, max, value) {
    val waveformImageProperty = SimpleObjectProperty<Image>()
    val thumbFillProperty = SimpleObjectProperty<Paint>(Paint.valueOf("#00000015"))
    val thumbLineColorProperty = SimpleObjectProperty<Paint>(Color.BLACK)
    val secondsToHighlightProperty = SimpleIntegerProperty(1)

    var waveformMinimapListener: ChangeListener<Image>? = null

    val player = SimpleObjectProperty<IAudioPlayer>()
    var reader: AudioFileReader? = null

    init {
        // initial height/width to prevent the control from otherwise growing indefinitely
        prefHeight = 10.0
        prefWidth = 50.0

        player.onChangeAndDoNow { player ->
            player?.let {
                reader = it.getAudioReader()
                setMax(it.getDurationInFrames().toDouble())
                it.seek(0)
            }
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return WaveformSliderSkin(this)
    }

    /**
     * Cleans up listeners to release memory usage.
     * Calls this method when leaving/undocking the view.
     */
    fun clearListeners() {
        if (waveformMinimapListener != null) {
            waveformImageProperty.removeListener(waveformMinimapListener)
        }
    }
}
