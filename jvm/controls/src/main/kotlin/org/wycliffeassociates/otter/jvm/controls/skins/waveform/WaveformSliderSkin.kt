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
package org.wycliffeassociates.otter.jvm.controls.skins.waveform

import com.sun.javafx.util.Utils
import javafx.beans.value.ChangeListener
import javafx.scene.Node
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import kotlin.math.max
import kotlin.math.min

class WaveformSliderSkin(val control: AudioSlider) : SkinBase<Slider>(control) {

    private val thumb = Rectangle(1.0, 1.0).apply {
        stroke = Color.BLACK
        strokeWidth = 1.0
        arcHeight = 10.0
        arcWidth = 10.0
    }
    private val playbackLine = Line(0.0, 0.0, 0.0, 1.0).apply {
        stroke = Color.BLACK
        strokeWidth = 1.0
    }
    private val markersHolder = Region()
    private val root = Region()

    private var imageViewDisposable: ImageView? = null

    init {
        children.clear()

        control.waveformMinimapListener = ChangeListener { _, oldValue, newValue ->
            newValue?.let { it ->
                val imageView = ImageView(it).apply {
                    fitHeightProperty().bind(root.heightProperty())
                    fitWidthProperty().bind(root.widthProperty())
                }
                imageViewDisposable = imageView
                root.getChildList()?.clear()
                root.add(imageView)
                root.add(thumb)
                root.add(playbackLine)

                markersHolder.apply {
                    prefHeightProperty().bind(root.heightProperty())
                    prefWidthProperty().bind(root.widthProperty())
                }
                root.add(markersHolder)
            }

            // clear minimap image when exiting marker app - free up memory
            if (newValue == null && oldValue != null) {
                imageViewDisposable?.image = null
            }
        }
        control.waveformImageProperty.addListener(control.waveformMinimapListener)

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
            placeMarkers()
        }
        control.markers.onChangeAndDoNow {
            placeMarkers()
        }
    }

    fun placeMarkers() {
        markersHolder.getChildList()?.clear()
        control.markers.forEach {
            markersHolder.add(createMarker(it))
        }
    }

    fun updateMarker(id: Int, position: Double) {
        val location = position * control.max

        val min = getMin(id)
        val max = getMax(id)
        val clamped = Utils.clamp(min, location, max)

        control.markers[id].location = clamped.toInt()
        placeMarkers()
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
        return control.reader?.let { it ->
            val secondsToHighlight = control.secondsToHighlightProperty.value
            val framesInHighlight = it.sampleRate * secondsToHighlight
            val framesPerPixel = it.totalFrames / max(control.widthProperty().value, 1.0)
            val pixelsInHighlight = max(framesInHighlight / framesPerPixel, 1.0)
            thumb.width = min(pixelsInHighlight, control.width)
            return pixelsInHighlight
        } ?: 0.0
    }

    private fun createMarker(cue: AudioCue): Node {
        val controlWidth = control.widthProperty().value
        val pos = (cue.location / control.max) * controlWidth
        var xFinal = min(pos, controlWidth)
        xFinal = max(xFinal, 0.0)

        val line = Line(0.0, 0.0, 0.0, 1.0).apply {
            stroke = Color.BLACK
            strokeWidth = 2.0
        }
        line.layoutX = xFinal
        line.layoutY = control.padding.top
        line.endYProperty().bind(
            root.heightProperty() - control.padding.top - control.padding.bottom
        )
        line.tooltip {
            text = cue.label
        }
        return line
    }

    private fun getMin(id: Int): Double {
        val placedMarkers = control.markers
        val previousMaker = if (id > 0) {
            placedMarkers[id - 1]
        } else {
            null
        }
        return previousMaker?.location?.toDouble() ?: 0.0
    }

    private fun getMax(id: Int): Double {
        val placedMarkers = control.markers
        val previousMaker = if (id < placedMarkers.size - 1) {
            placedMarkers[id + 1]
        } else {
            null
        }
        return previousMaker?.location?.toDouble() ?: control.max
    }
}
