package org.wycliffeassociates.otter.jvm.markerapp.app.view

import com.sun.glass.ui.Screen
import javafx.animation.AnimationTimer
import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Paint
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.utils.fitToSize
import org.wycliffeassociates.otter.jvm.controls.utils.fitToWidth
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MainWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerTrack
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.PlaceMarkerLayer
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.TimecodeHolder
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformFrame(
    markerTrack: MarkerTrack,
    mainWaveform: MainWaveform,
    playedOverlay: Rectangle,
    timecodeHolder: TimecodeHolder,
    viewModel: VerseMarkerViewModel
) : BorderPane() {

    init {
        fitToParentSize()
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        with(this) {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            top {
                region {
                    styleClass.add("vm-waveform-frame__top-track")
                    add(markerTrack)
                }
            }

            center {
                region {
                    mainWaveform.fitToSize(this@region)

                    stackpane {
                        styleClass.add("vm-waveform-frame__center")
                        alignment = Pos.CENTER

                        fitToParentWidth()
                        fitToParentHeight()

                        add(mainWaveform)
                        add(
                            playedOverlay.apply {
                                styleClass.add("vm-waveform-holder--played")
                                heightProperty().bind(this@region.heightProperty())
                                widthProperty().bind(
                                    Bindings.min(
                                        viewModel.positionProperty.times(this@region.widthProperty() / Screen.getMainScreen().width),
                                        this@region.widthProperty().divide(2)
                                    )
                                )
                                translateXProperty().bind(-widthProperty() / 2)
                            }
                        )
                        add(
                            Line(0.0, 0.0, 0.0, 0.0).apply {
                                endYProperty().bind(this@region.heightProperty())
                                styleClass.add("vm-playback-line")
                            }
                        )
                        add(PlaceMarkerLayer(viewModel))
                    }
                }
            }

            bottom {
                region {
                    styleClass.add("vm-waveform-frame__bottom-track")
                    stackpane {
                        timecodeHolder.fitToWidth(this@region)
                        add(timecodeHolder)
                        add(
                            Line(0.0, 0.0, 0.0, 0.0).apply {
                                styleClass.add("vm-playback-line")
                                endYProperty().bind(this@region.heightProperty())
                            }
                        )
                    }
                }
            }
        }
    }
}

