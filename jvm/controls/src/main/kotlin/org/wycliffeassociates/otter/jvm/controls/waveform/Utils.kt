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

import javafx.animation.AnimationTimer
import javafx.scene.effect.ColorAdjust
import org.wycliffeassociates.otter.common.data.ColorTheme

fun adjustWaveformColorByTheme(theme: ColorTheme, adjust: ColorAdjust) {
    when(theme) {
        ColorTheme.LIGHT -> {
            adjust.brightness = 0.0
            adjust.contrast = 0.0
        }
        ColorTheme.DARK -> {
            adjust.brightness = -0.65
            adjust.contrast = 0.5
        }
        else -> {}
    }
}

fun startAnimationTimer(onRefresh: () -> Unit): AnimationTimer {
    return object : AnimationTimer() {
        override fun handle(currentNanoTime: Long) {
            onRefresh()
        }
    }.apply { start() }
}