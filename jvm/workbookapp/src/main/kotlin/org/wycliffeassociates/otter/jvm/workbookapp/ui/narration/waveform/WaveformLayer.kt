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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform

import com.sun.glass.ui.Screen
import javafx.event.EventTarget
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.ScrollBar
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.narration.CanvasFragment
import tornadofx.*


// Thing that is the container for the Waveform and VolumeBar
class WaveformLayer : BorderPane() {

    private var waveformCanvas = CanvasFragment()
    private var volumeBarCanvas = CanvasFragment()
    private val volumeBarWidth = 25
    private val maxScreenWidth = Screen.getMainScreen().width.toDouble()

    init {
        waveformCanvas.let {
            prefWidthProperty().bind(this.widthProperty().minus(volumeBarWidth))
            maxWidth(maxScreenWidth - volumeBarWidth)
            style {
                backgroundColor += c("#E5E8EB")
            }
        }

        center = waveformCanvas

        right = hbox {
            prefWidth = 25.0
            volumeBarCanvas.let {
                style {
                    backgroundColor += c("#001533")
                }
            }
            volumeBarCanvas.prefWidthProperty().bind(this.widthProperty())
            add(volumeBarCanvas)
        }
    }

    fun getWaveformCanvas(): Canvas {
        return waveformCanvas.getCanvas()
    }

    fun getWaveformContext(): GraphicsContext {
        return waveformCanvas.getContext()
    }

    fun getVolumeCanvas(): Canvas {
        return volumeBarCanvas.getCanvas()
    }

    fun getVolumeBarContext(): GraphicsContext {
        return volumeBarCanvas.getContext()
    }
}

fun EventTarget.narration_waveform(
    op: WaveformLayer.() -> Unit = {}
): WaveformLayer {
    val waveformLayer = WaveformLayer()
    opcr(this, waveformLayer, op)
    return waveformLayer
}