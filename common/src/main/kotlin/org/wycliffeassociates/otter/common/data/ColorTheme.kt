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
package org.wycliffeassociates.otter.common.data

const val WAV_COLOR_LIGHT = "#66768B"
const val WAV_BACKGROUND_COLOR_LIGHT = "#FFFFFF"
const val WAV_COLOR_DARK = "#808080"
const val WAV_BACKGROUND_COLOR_DARK = "#343434"

enum class ColorTheme(val titleKey: String, val styleClass: String = "") {
    LIGHT("light", "light-theme"),
    DARK("dark", "dark-theme"),
    SYSTEM("system");
}

data class WaveformColors(val wavColorHex: String, val backgroundColorHex: String)

fun getWaveformColors(theme: ColorTheme): WaveformColors {
    return when (theme) {
        ColorTheme.LIGHT -> {
            WaveformColors(WAV_COLOR_LIGHT, WAV_BACKGROUND_COLOR_LIGHT)
        }

        ColorTheme.DARK -> {
            WaveformColors(WAV_COLOR_DARK, WAV_BACKGROUND_COLOR_DARK)
        }

        else -> {
            WaveformColors(WAV_COLOR_LIGHT, WAV_BACKGROUND_COLOR_LIGHT)
        }
    }
}