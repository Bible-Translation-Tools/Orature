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
package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import tornadofx.*

class WaveformOverlay : StackPane() {

    val playbackPositionProperty = SimpleDoubleProperty(0.0)

    init {
        isMouseTransparent = true
        alignment = Pos.BOTTOM_CENTER
        
        add(
            Line(0.0, 40.0, 0.0, 0.0).apply {
                managedProperty().set(false)
                startXProperty().bind(this@WaveformOverlay.widthProperty().divide(2))
                endXProperty().bind(this@WaveformOverlay.widthProperty().divide(2))
                endYProperty().bind(this@WaveformOverlay.heightProperty())
                styleClass.add("scrolling-waveform__playback-line")
            }
        )
    }
}
