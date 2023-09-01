package org.wycliffeassociates.otter.jvm.controls.skins.slider

import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import kotlin.math.max
import kotlin.math.min

class AudioSliderSkin(val control: AudioSlider) : SkinBase<Slider>(control) {
    private val thumb = Rectangle(10.0, 10.0).apply {
        addClass("wa-audio-slider-thumb")
    }
    private val root = HBox().apply {
        addClass("audio-scroll-bar__track")
        add(thumb)
    }
    init {
        control.secondsToHighlightProperty.onChangeAndDoNow {
            resizeThumbWidth()
        }
        control.valueProperty().onChangeAndDoNow { moveThumb() }
        control.widthProperty().onChangeAndDoNow {
            moveThumb()
            resizeThumbWidth()
        }
        thumb.setOnMouseDragged {
            val x = control.sceneToLocal(it.sceneX, it.sceneY).x
            val pos = (x / control.width) * control.max
            control.valueProperty().set(pos)
            control.currentPositionProperty.set(control.value)
        }

        children.setAll(root)
    }

    private fun resizeThumbWidth(): Double {
        val pixelsInHighlight = control.pixelsInHighlight.value.invoke(control.width)
        thumb.width = min(pixelsInHighlight, control.width)
        return pixelsInHighlight
    }

    private fun moveThumb() {
        val controlWidth = control.widthProperty().value
        val pos = (control.valueProperty().value / control.max) * control.widthProperty().value
        var xFinalThumb = min(pos, controlWidth - thumb.width / 2.0)
        val xCurrent = thumb.layoutX

        xFinalThumb -= thumb.width / 2.0
        xFinalThumb = max(xFinalThumb, 0.0)

        thumb.translateX = xFinalThumb - xCurrent
    }
}