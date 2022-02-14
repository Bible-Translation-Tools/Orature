package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import tornadofx.add
import tornadofx.div
import tornadofx.minus
import tornadofx.unaryMinus

class WaveformOverlay : StackPane() {

    val playbackPositionProperty = SimpleDoubleProperty(0.0)

    init {
        isMouseTransparent = true
        alignment = Pos.BOTTOM_CENTER

        add(
            Rectangle().apply {
                styleClass.add("vm-waveform-holder--played")
                heightProperty().bind(this@WaveformOverlay.heightProperty().minus(90.0))
                widthProperty().bind(
                    Bindings.min(
                        playbackPositionProperty,
                        this@WaveformOverlay.widthProperty().divide(2)
                    )
                )
                translateYProperty().set(-50.0)
                translateXProperty().bind(-widthProperty() / 2)
            }
        )
        add(
            Line(0.0, 40.0, 0.0, 0.0).apply {
                endYProperty().bind(this@WaveformOverlay.heightProperty())
                styleClass.add("vm-playback-line")
            }
        )
    }
}
