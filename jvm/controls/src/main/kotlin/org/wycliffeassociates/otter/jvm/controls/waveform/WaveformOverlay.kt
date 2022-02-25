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

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import tornadofx.add
import tornadofx.div
import tornadofx.minus
import tornadofx.unaryMinus

class WaveformOverlay : StackPane() {

    val playbackPositionProperty = SimpleDoubleProperty(0.0)

    init {
        isMouseTransparent = true
        alignment = Pos.BOTTOM_CENTER

        add(
            Rectangle().apply {
                styleClass.add("scrolling-waveform-holder--played")
                heightProperty().bind(this@WaveformOverlay.heightProperty().minus(90.0))
                widthProperty().bind(
                    Bindings.min(
                        playbackPositionProperty,
                        this@WaveformOverlay.widthProperty().divide(2)
                    )
                )
                translateYProperty().set(-50.0)
                translateXProperty().bind(-widthProperty() / 2)
            }
        )
        add(
            Line(0.0, 40.0, 0.0, 0.0).apply {
                endYProperty().bind(this@WaveformOverlay.heightProperty())
                styleClass.add("scrolling-waveform__playback-line")
            }
        )
    }
}
