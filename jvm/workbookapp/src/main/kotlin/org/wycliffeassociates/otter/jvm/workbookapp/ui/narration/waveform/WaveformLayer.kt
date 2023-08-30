package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform

import com.sun.glass.ui.Screen
import javafx.event.EventTarget
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
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
                backgroundColor += c("#FF0000")
            }
        }

        center = waveformCanvas

        hbox {
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