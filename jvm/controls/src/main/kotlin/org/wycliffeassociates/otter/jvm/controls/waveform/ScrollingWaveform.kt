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

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.image.Image
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.controllers.ScrollSpeed
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.ScrollingWaveformSkin

open class ScrollingWaveform : Control() {
    val positionProperty = SimpleDoubleProperty(0.0)
    val themeProperty = SimpleObjectProperty(ColorTheme.LIGHT)

    var onWaveformClicked: () -> Unit = {}
    var onWaveformDragReleased: (Double) -> Unit = {}
    var onRewind: ((ScrollSpeed) -> Unit) = {}
    var onFastForward: ((ScrollSpeed) -> Unit) = {}
    var onToggleMedia: () -> Unit = {}
    var onResumeMedia: () -> Unit = {}

    fun addWaveformImage(image: Image) {
        (skin as ScrollingWaveformSkin).addWaveformImage(image)
    }

    fun freeImages() {
        (skin as ScrollingWaveformSkin).freeImages()
    }

    override fun createDefaultSkin(): Skin<*> {
        return ScrollingWaveformSkin(this)
    }
}
