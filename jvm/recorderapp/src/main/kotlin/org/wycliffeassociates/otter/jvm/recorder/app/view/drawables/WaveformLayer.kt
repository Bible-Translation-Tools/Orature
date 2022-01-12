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
package org.wycliffeassociates.otter.jvm.recorder.app.view.drawables

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer

private const val USHORT_SIZE = 65535.0

class WaveformLayer(private val renderer: ActiveRecordingRenderer) : Drawable {

    override fun draw(context: GraphicsContext, canvas: Canvas) {
        context.stroke = Paint.valueOf("#1A1A1A")
        context.lineWidth = 1.0

        val buffer = renderer.floatBuffer.array
        var i = 0
        var x = 0.0
        while (i < buffer.size) {
            context.strokeLine(
                x,
                scaleAmplitude(buffer[i].toDouble(), canvas.height),
                x,
                scaleAmplitude(buffer[i + 1].toDouble(), canvas.height)
            )
            i += 2
            x += 1
        }
    }

    // 16 bit audio range is -32,768 to 32,767, or 65535 (size of unsigned short)
    // This scales the sample to fit within the canvas height, and moves the
    // sample down (-y translate) by half the height
    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height * (sample / USHORT_SIZE) + height / 2
    }
}
