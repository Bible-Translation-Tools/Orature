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
package org.wycliffeassociates.otter.jvm.recorder.app.view.drawables

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint

class BaseWaveLine : Drawable {
    override fun draw(context: GraphicsContext, canvas: Canvas) {
        context.fill = Paint.valueOf("#1A1A1A")
        context.stroke = Paint.valueOf("#1A1A1A")
        context.lineWidth = 1.0
        context.strokeLine(0.0, canvas.height / 2.0, canvas.width, canvas.height / 2.0)
    }
}