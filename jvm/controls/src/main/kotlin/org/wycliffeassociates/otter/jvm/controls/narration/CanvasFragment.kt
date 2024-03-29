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
package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.controls.canvas.ResizableCanvas
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import tornadofx.*

class CanvasFragment : StackPane() {
    private val cvs = ResizableCanvas()
    private val ctx = cvs.graphicsContext2D

    fun getCanvas(): Canvas {
        return cvs
    }

    fun getContext(): GraphicsContext {
        return ctx
    }

    init {
        addClass("waveform")
        alignment = Pos.TOP_RIGHT

        add(cvs)
    }
}
