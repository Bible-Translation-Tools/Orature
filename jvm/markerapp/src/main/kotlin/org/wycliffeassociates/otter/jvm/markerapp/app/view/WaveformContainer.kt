package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.animation.AnimationTimer
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MainWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerTrack
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.TimecodeHolder
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class WaveformContainer : Fragment() {

    val viewModel: VerseMarkerViewModel by inject()
    val mainWaveform: MainWaveform
    val playedOverlay = Rectangle()
    val markerTrack: MarkerTrack
    val timecodeHolder: TimecodeHolder

    init {
        markerTrack = MarkerTrack(viewModel).apply { prefWidth = viewModel.imageWidth }
        timecodeHolder = TimecodeHolder(viewModel, 50.0)
        mainWaveform = MainWaveform(viewModel, viewModel.audioPlayer.getAudioReader()!!)

        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                if (mainWaveform.image != null) {
                    viewModel.calculatePosition()
                }
            }
        }.start()
    }

    override val root = WaveformFrame(
        markerTrack,
        mainWaveform,
        playedOverlay,
        timecodeHolder,
        viewModel
    )
}
