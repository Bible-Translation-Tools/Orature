package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.glass.ui.Screen
import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import tornadofx.style

interface ViewPortScrollable {
    fun scrollTo(x: Double)
}

class MainWaveform(
    reader: AudioFileReader, val width: Int, val height: Int
): ImageView(), ViewPortScrollable {

    init {
        WaveformImageBuilder(
            wavColor = Color.web("#0A337390"),
            background = Color.web("#F7FAFF")
        ).build(
            reader,
            fitToAudioMax = false,
            width = width,
            height = height
        ).subscribe { image ->
            imageProperty().set(image)
        }

        style {
            backgroundColor += Paint.valueOf("#0a337333")
        }
    }

    override fun scrollTo(x: Double) {
        val ht = Screen.getMainScreen().platformHeight
        val wd = Screen.getMainScreen().platformWidth
        viewport = Rectangle2D(x, 0.0, wd.toDouble(), ht.toDouble())
    }
}
