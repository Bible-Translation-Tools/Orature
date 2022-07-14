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
import javafx.geometry.NodeOrientation
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.controllers.ScrollSpeed
import tornadofx.*

open class ScrollingWaveform : StackPane() {

    val positionProperty = SimpleDoubleProperty(0.0)
    val themeProperty = SimpleObjectProperty(ColorTheme.LIGHT)

    var onWaveformClicked: () -> Unit = {}
    var onWaveformDragReleased: (Double) -> Unit = {}
    var onRewind: ((ScrollSpeed) -> Unit) = {}
    var onFastForward: ((ScrollSpeed) -> Unit) = {}
    var onToggleMedia: () -> Unit = {}
    var onResumeMedia: () -> Unit = {}

    private val waveformFrame: WaveformFrame

    init {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

        add(MarkerViewBackground())
        waveformFrame = WaveformFrame().apply {
            framePositionProperty.bind(positionProperty)
            onWaveformClicked { onWaveformClicked() }
            onWaveformDragReleased {
                onWaveformDragReleased(it)
            }
            onRewind(onRewind)
            onFastForward(onFastForward)
            onToggleMedia(onToggleMedia)
            onResumeMedia(onResumeMedia)

            focusedProperty().onChange {
                togglePseudoClass("active", it)
            }
        }
        add(waveformFrame)
        add(WaveformOverlay().apply { playbackPositionProperty.bind(positionProperty) })
    }

    fun freeImages() {
        waveformFrame.freeImages()
    }

    fun addWaveformImage(image: Image) {
        waveformFrame.addImage(image)
    }
}
