package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.utils.fitToHeight
import org.wycliffeassociates.otter.jvm.controls.utils.fitToSize
import org.wycliffeassociates.otter.jvm.controls.utils.fitToWidth
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.*
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class WaveformFrame(
    markerTrack: MarkerTrackControl,
    mainWaveform: MainWaveform,
    timecodeHolder: TimecodeHolder,
    viewModel: VerseMarkerViewModel
) : BorderPane() {

    init {
        fitToParentSize()
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        with(this) {
            translateXProperty().bind(
                viewModel
                    .positionProperty
                    .negate()
                    .plus(
                        this@WaveformFrame.widthProperty().divide(2.0)
                    )
            )

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
                    mainWaveform.fitToHeight(this@region)

                    stackpane {
                        styleClass.add("vm-waveform-frame__center")
                        alignment = Pos.CENTER

                        fitToParentHeight()
                        add(mainWaveform)
                    }
                }
            }

            bottom {
                region {
                    styleClass.add("vm-waveform-frame__bottom-track")
                    add(timecodeHolder)
                }
            }
        }
    }
}
