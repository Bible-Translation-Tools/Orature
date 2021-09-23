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
package org.wycliffeassociates.otter.jvm.controls.skins.waveform

import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import kotlin.math.max
import kotlin.math.min

class WaveformSliderSkin(val control: AudioSlider) : SkinBase<Slider>(control) {

    val thumb = Rectangle(1.0, 1.0).apply {
        stroke = Color.BLACK
        strokeWidth = 1.0
        arcHeight = 10.0
        arcWidth = 10.0
    }
    val playbackLine = Line(0.0, 0.0, 0.0, 1.0).apply {
        stroke = Color.BLACK
        strokeWidth = 1.0
    }

    val root = Region()

    init {
        children.clear()

        control.waveformImageProperty.onChangeAndDoNow {
            it?.let { images ->
//                val imageView = ImageView(images).apply {
//                    fitHeightProperty().bind(root.heightProperty())
//                    fitWidthProperty().bind(root.widthProperty())
//                }

                val hbox = HBox()
                hbox.children.setAll(
                    images.map { img ->
                        ImageView(img).apply {
                            fitHeightProperty().bind(root.heightProperty())
//                            fitWidth = root.width / images.size
                            fitWidthProperty().bind(
                                root.widthProperty()
                                    .divide(control.imageWidth)
                                    .multiply(img.width)
                            )
                        }
                    })
                root.getChildList()?.clear()
                root.add(hbox)
                root.add(thumb)
                root.add(playbackLine)
            }
        }

        children.add(root)

        control.thumbFillProperty.onChangeAndDoNow {
            if (it != null) {
                thumb.fill = it
            }
        }
        control.thumbLineColorProperty.onChangeAndDoNow {
            if (it != null) {
                thumb.stroke = it
            }
        }
        control.secondsToHighlightProperty.onChange { resizeThumbWidth() }
        thumb.layoutY = control.padding.top
        playbackLine.layoutY = control.padding.top
        thumb.heightProperty().bind(
            root.heightProperty() - control.padding.top - control.padding.bottom
        )
        playbackLine.endYProperty().bind(
            root.heightProperty() - control.padding.top - control.padding.bottom
        )
        control.valueProperty().onChange { moveThumb() }
        control.widthProperty().onChangeAndDoNow {
            moveThumb()
            resizeThumbWidth()
        }
    }

    private fun moveThumb() {
        val controlWidth = control.widthProperty().value
        val pos = (control.valueProperty().value / control.max) * control.widthProperty().value
        var xFinal = min(pos, controlWidth)
        var xFinalThumb = min(pos, controlWidth - thumb.width / 2.0)
        val xCurrent = thumb.layoutX

        xFinal = max(xFinal, 0.0)
        xFinalThumb -= thumb.width / 2.0
        xFinalThumb = max(xFinalThumb, 0.0)

        thumb.translateX = xFinalThumb - xCurrent
        playbackLine.translateX = xFinal - xCurrent
    }

    private fun resizeThumbWidth(): Double {
        return control.player.value?.getAudioReader()?.let { reader ->
            reader?.let {
                val secondsToHighlight = control.secondsToHighlightProperty.value
                val framesInHighlight = it.sampleRate * secondsToHighlight
                val framesPerPixel = it.totalFrames / max(control.widthProperty().value, 1.0)
                val pixelsInHighlight = max(framesInHighlight / framesPerPixel, 1.0)
                thumb.width = min(pixelsInHighlight, control.width)
                return pixelsInHighlight
            }
        } ?: 0.0
    }
}
