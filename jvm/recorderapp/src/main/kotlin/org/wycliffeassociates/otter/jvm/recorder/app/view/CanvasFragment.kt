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
package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.recorder.app.view.drawables.Drawable
import tornadofx.*

class CanvasFragment : StackPane() {

    private val drawables = arrayListOf<Drawable>()

    private val cvs = ResizableCanvas()
    private val ctx = cvs.graphicsContext2D

    init {
        addClass("waveform")
        alignment = Pos.TOP_LEFT

        add(cvs)

        cvs.widthProperty().addListener { _ -> draw() }
        cvs.heightProperty().addListener { _ -> draw() }

        draw()
    }

    fun addDrawable(drawable: Drawable) {
        drawables.add(drawable)
    }

    fun draw() {
        ctx.clearRect(0.0, 0.0, cvs.width, cvs.height)
        var i = 0
        while (i < drawables.size) {
            drawables[i].draw(ctx, cvs)
            i++
        }
    }
}