package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import javafx.scene.paint.Paint
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class WaveformOverlay(val viewModel: VerseMarkerViewModel) : StackPane() {

    init {
        isMouseTransparent = true
        alignment = Pos.BOTTOM_CENTER

        add(
            Rectangle().apply {
                styleClass.add("vm-waveform-holder--played")
                heightProperty().bind(this@WaveformOverlay.heightProperty().minus(90.0))
                widthProperty().bind(
                    Bindings.min(
                        viewModel.positionProperty,
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
