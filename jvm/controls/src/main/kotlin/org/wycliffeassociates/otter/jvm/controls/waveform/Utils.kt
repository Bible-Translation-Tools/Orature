package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.scene.effect.ColorAdjust
import org.wycliffeassociates.otter.common.data.ColorTheme

fun adjustWaveformImageByTheme(theme: ColorTheme, adjust: ColorAdjust) {
    when(theme) {
        ColorTheme.LIGHT -> {
            adjust.brightness = 0.0
            adjust.contrast = 0.0
        }
        ColorTheme.DARK -> {
            adjust.brightness = -0.65
            adjust.contrast = 0.5
        }
    }
}