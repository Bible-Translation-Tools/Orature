package org.wycliffeassociates.otter.jvm.controls.skins.waveform

import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import kotlin.math.max
import kotlin.math.min

class WaveformSliderSkin(val control: AudioSlider) : SkinBase<Slider>(control) {

    private val logger = LoggerFactory.getLogger(WaveformImageBuilder::class.java)

    val waveformImage = WaveformImageBuilder()
    val thumb = Rectangle(1.0, 1.0).apply {
        stroke = Color.BLACK
        strokeWidth = 1.0
        arcHeight = 10.0
        arcWidth = 10.0
    }
    val root = Region()

    init {
        children.clear()

        control.player.onChangeAndDoNow { _player ->
            _player?.let { player ->
                player.getAudioReader()?.let { reader ->
                    reader.seek(0)
                    waveformImage
                        .build(reader, fitToAudioMax = true)
                        .doOnError { e ->
                            logger.error("Error in building waveform image", e)
                        }
                        .subscribe { image: Image ->
                            val imageView = ImageView(image).apply {
                                fitHeightProperty().bind(root.heightProperty())
                                fitWidthProperty().bind(root.widthProperty())
                            }
                            reader.seek(0)
                            root.getChildList()?.clear()
                            root.add(imageView)
                            root.add(thumb)
                        }
                }
            }
        }

        children.add(root)

        control.thumbFillProperty.onChangeAndDoNow {
            if (it != null) {
                thumb.fill = it
            }
        }
        control.thumbLineColorProperty.onChangeAndDoNow {
            if (it != null) {
                thumb.stroke = it
            }
        }
        control.secondsToHighlightProperty.onChange { resizeThumbWidth() }
        thumb.layoutY = control.padding.top
        thumb.heightProperty().bind(root.heightProperty() - control.padding.top - control.padding.bottom)
        control.valueProperty().onChange { moveThumb() }
        control.widthProperty().onChangeAndDoNow {
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
        return control.player.value?.getAudioReader()?.let { reader ->
            reader?.let {
                val secondsToHighlight = control.secondsToHighlightProperty.value
                val framesInHighlight = it.sampleRate * secondsToHighlight
                val framesPerPixel = it.totalFrames / max(control.widthProperty().value, 1.0)
                val pixelsInHighlight = max(framesInHighlight / framesPerPixel, 1.0)
                thumb.width = pixelsInHighlight
                return pixelsInHighlight
            }
        } ?: 0.0
    }
}
