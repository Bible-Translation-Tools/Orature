package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.animation.AnimationTimer
import javafx.scene.layout.Priority
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.*
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformContainer : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()
    val mainWaveform: MainWaveform
    // val markerTrack: MarkerTrack
    val markerTrack2: MarkerTrackControl
    val timecodeHolder: TimecodeHolder

    init {
        // markerTrack = MarkerTrack(viewModel).apply { prefWidth = viewModel.imageWidth }
        markerTrack2 = MarkerTrackControl(viewModel.markers.markerTotal, viewModel).apply { prefWidth = viewModel.imageWidth }
        timecodeHolder = TimecodeHolder(viewModel, 50.0)
        mainWaveform = MainWaveform(viewModel)

        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                if (mainWaveform.image != null) {
                    viewModel.calculatePosition()
                }
            }
        }.start()
    }

    override val root =
        stackpane {
            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            add(MarkerViewBackground())
            add(
                WaveformFrame(
                    markerTrack2,
                    mainWaveform,
                    timecodeHolder,
                    viewModel
                )
            )
            add(WaveformOverlay(viewModel))
            add(PlaceMarkerLayer(viewModel))
        }
}
