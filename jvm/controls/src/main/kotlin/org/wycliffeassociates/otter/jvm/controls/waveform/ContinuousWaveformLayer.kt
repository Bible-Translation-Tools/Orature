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

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer

private const val USHORT_SIZE = 65535.0

class ContinuousWaveformLayer(private val renderer: ActiveRecordingRenderer) : Drawable {

    override fun draw(context: GraphicsContext, canvas: Canvas) {
        val audioData = renderer.audioData

        context.stroke = Paint.valueOf("#015AD990")
        context.lineWidth = 1.0
        canvas.width = audioData.size.toDouble() / 2

        var i = audioData.size - 1
        var x = canvas.width

        while (i > 0) {
            val y1 = scaleAmplitude(audioData[i].toDouble(), canvas.height)
            val y2 = scaleAmplitude(audioData[i - 1].toDouble(), canvas.height)

            context.strokeLine(x, y1, x, y2)
            i -= 2
            x -= 1
        }
    }

    // 16 bit audio range is -32,768 to 32,767, or 65535 (size of unsigned short)
    // This scales the sample to fit within the canvas height, and moves the
    // sample down (-y translate) by half the height
    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height * (sample / USHORT_SIZE) + height / 2
    }
}