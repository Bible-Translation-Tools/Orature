package org.wycliffeassociates.otter.jvm.controls.skins.waveform

import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import tornadofx.*
import kotlin.math.max
import kotlin.math.min

class WaveformSliderSkin(val control: AudioSlider) : SkinBase<Slider>(control) {

    val waveformImage = WaveformImageBuilder()
    val thumb = Rectangle(1.0, 1.0).apply {
        stroke = Color.BLACK
        strokeWidth = 1.0
        arcHeight = 10.0
        arcWidth = 10.0
    }

    init {
        children.clear()
        val region = Region()
        waveformImage.build(
            control.file,
            0,
            Color.GRAY,
            Color.WHITE
        ).subscribe { image ->
            region.add(
                ImageView(image).apply {
                    fitHeightProperty().bind(region.heightProperty())
                    fitWidthProperty().bind(region.widthProperty())
                }
            )
            region.add(thumb)
        }
        children.add(region)
        thumb.fill = Paint.valueOf("#00000015")
        thumb.width = resizeThumbWidth()
        thumb.layoutY = 5.0

        thumb.heightProperty().bind(region.heightProperty() - 10)
        control.valueProperty().onChange { moveThumb() }
        control.widthProperty().onChange {
            moveThumb()
            resizeThumbWidth()
        }
    }

    private fun moveThumb() {
        val xFinal = min(
            (control.valueProperty().value / control.max) * control.widthProperty().value,
            control.widthProperty().value - thumb.widthProperty().value
        )
        val xCurrent = thumb.layoutX
        thumb.translateX = xFinal - xCurrent
    }

    private fun resizeThumbWidth(): Double {
        val samplesOnScreen = 10
        val framesInHighlight = control.file.sampleRate * samplesOnScreen
        val framesPerPixel = control.file.totalFrames / max(control.widthProperty().value, 1.0)
        val pixelsInHighlight = max(framesInHighlight / framesPerPixel, 1.0)
        thumb.width = pixelsInHighlight
        return pixelsInHighlight
    }
}
