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
package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.sun.glass.ui.Screen
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.SECONDS_ON_SCREEN

private const val EPSILON = 10
private const val MS_IN_SECOND = 1000
private const val SAMPLE_RATE = DEFAULT_SAMPLE_RATE

internal fun pixelsInSecond(width: Double, durationMs: Int): Int {
    val msInPixels = durationMs / width
    return (MS_IN_SECOND / msInPixels + EPSILON).toInt()
}

internal fun positionToMs(x: Int, width: Double, durationMs: Int): Int {
    val msInPixels = durationMs / width
    return (x * msInPixels).toInt()
}

internal fun pixelsToFrames(pixels: Double): Int {
    val framesOnScreen = SECONDS_ON_SCREEN * SAMPLE_RATE
    val framesInPixel = framesOnScreen / Screen.getMainScreen().platformWidth.toDouble()
    return (pixels * framesInPixel).toInt()
}

internal fun framesToPixels(frames: Int): Int {
    val framesOnScreen = SECONDS_ON_SCREEN * SAMPLE_RATE
    val framesInPixel = framesOnScreen / Screen.getMainScreen().platformWidth.toDouble()
    return (frames / framesInPixel).toInt()
}
