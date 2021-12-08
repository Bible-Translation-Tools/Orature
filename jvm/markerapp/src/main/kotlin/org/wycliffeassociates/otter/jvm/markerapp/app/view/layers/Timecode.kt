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
package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.markerapp.app.view.pixelsInSecond
import org.wycliffeassociates.otter.jvm.markerapp.app.view.positionToMs
import java.lang.Math.floor
import java.util.concurrent.TimeUnit

class Timecode(width: Double, height: Double) : Canvas(width, height) {

    private val ctx = graphicsContext2D

    // an epsilon offset so the tick marks don't accidentally fall inside the same second
    // ie: 0:00 0:00 0:02 instead of 0:00 0:01 0:02
    private val epsilon = 10

    private val spacing = 5.0
    private val padding = 10.0

    init {
        styleClass.add("vm-timecode")
    }

    fun drawTimecode(durationMs: Int): Image {
        ctx.fill = Color.WHITE
        ctx.fillRect(0.0, 0.0, width, height)
        ctx.fill = Color.BLACK
        for (i in 0 until width.toInt() step pixelsInSecond(width, durationMs)) {
            ctx.strokeLine(i.toDouble(), floor(height / 2), i.toDouble(), floor(height - 1.0))
            if (i + epsilon < width) {
                val text = msToDisplayString(positionToMs((i - 1), width, durationMs))
                ctx.fillText(text, i + spacing, floor(height - padding))
            }
        }
        val img = WritableImage(width.toInt(), height.toInt())
        snapshot(SnapshotParameters(), img)
        return img
    }

    fun msToDisplayString(ms: Int): String {
        val minute = TimeUnit.MILLISECONDS.toMinutes(ms.toLong())
        val second = TimeUnit.MILLISECONDS.toSeconds(ms.toLong() - TimeUnit.MINUTES.toMillis(minute))

        return String.format("%02d:%02d", minute, second)
    }
}
