package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.animation.AnimationTimer
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.*
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformContainer : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()
    val mainWaveform: MainWaveform
    val markerTrack: MarkerTrackControl
    val timecodeHolder: TimecodeHolder

    init {
        markerTrack = MarkerTrackControl(viewModel.markers.markers, viewModel.markers.highlightState).apply {
            prefWidth = viewModel.imageWidth
            viewModel.markers.markerCountProperty.onChange {
                refreshMarkers()
            }
        }
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
                    markerTrack,
                    mainWaveform,
                    timecodeHolder,
                    viewModel
                )
            )
            add(WaveformOverlay(viewModel))
            add(PlaceMarkerLayer(viewModel))
        }
}
