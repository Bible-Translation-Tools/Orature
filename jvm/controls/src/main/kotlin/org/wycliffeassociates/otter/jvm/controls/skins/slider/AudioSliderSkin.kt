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
package org.wycliffeassociates.otter.jvm.controls.skins.slider

import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import kotlin.math.max
import kotlin.math.min

class AudioSliderSkin(val control: AudioSlider) : SkinBase<Slider>(control) {
    private val thumb = Rectangle(10.0, 10.0).apply {
        addClass("wa-audio-slider-thumb")
    }
    private val root = HBox().apply {
        addClass("audio-scroll-bar__track")
        add(thumb)
    }
    init {
        control.player.onChangeAndDoNow {
            resizeThumbWidth()
        }
        control.secondsToHighlightProperty.onChangeAndDoNow {
            resizeThumbWidth()
        }
        control.valueProperty().onChangeAndDoNow { moveThumb() }
        control.widthProperty().onChangeAndDoNow {
            resizeThumbWidth()
            moveThumb()
        }
        thumb.setOnMouseDragged {
            val x = control.sceneToLocal(it.sceneX, it.sceneY).x
            val pos = (x / control.width) * control.max
            control.valueProperty().set(pos)
            control.currentPositionProperty.set(control.value)
        }

        children.setAll(root)
    }

    private fun resizeThumbWidth(): Double {
        val pixelsInHighlight = control.pixelsInHighlight.value.invoke(control.width)
        thumb.width = min(pixelsInHighlight, control.width)
        return pixelsInHighlight
    }

    private fun moveThumb() {
        val controlWidth = control.width
        val pos = (control.value / control.max) * control.width
        var xFinalThumb = min(pos, controlWidth - thumb.width / 2.0)
        val xCurrent = thumb.layoutX

        xFinalThumb -= thumb.width / 2.0
        xFinalThumb = max(xFinalThumb, 0.0)

        thumb.translateX = xFinalThumb - xCurrent
    }
}