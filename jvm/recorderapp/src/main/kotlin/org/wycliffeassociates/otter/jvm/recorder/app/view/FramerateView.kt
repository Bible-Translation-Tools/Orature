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

import javafx.animation.AnimationTimer
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.paint.Paint

class FramerateView : Label() {

    private val frameTimes = LongArray(100)
    private var frameTimeIndex = 0
    private var arrayFilled = false

    private val builder = StringBuilder("FPS: 000")

    // code from:
    // https://stackoverflow.com/questions/28287398/what-is-the-preferred-way-of-getting-the-frame-rate-of-a-javafx-application
    val at = object : AnimationTimer() {

        override fun handle(currentNanoTime: Long) {
            val oldFrameTime = frameTimes[frameTimeIndex]
            frameTimes[frameTimeIndex] = currentNanoTime
            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.size
            if (frameTimeIndex == 0) {
                arrayFilled = true
            }
            if (arrayFilled) {
                val elapsedNanos = currentNanoTime - oldFrameTime
                val elapsedNanosPerFrame = elapsedNanos / frameTimes.size
                val frameRate = 1_000_000_000 / elapsedNanosPerFrame
                builder.replace(5, 8, frameRate.toString())
                text = builder.toString()
            }
        }
    }.start()

    init {
        prefHeight = 50.0
        prefWidth = 100.0
        alignment = Pos.TOP_LEFT
        textFill = Paint.valueOf("#00FF00")
    }
}