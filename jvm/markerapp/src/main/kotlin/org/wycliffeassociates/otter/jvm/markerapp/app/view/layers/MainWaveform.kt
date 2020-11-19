package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow

private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#F7FAFF"

class MainWaveform(val viewModel: VerseMarkerViewModel) : ImageView() {

    init {
        styleClass.add("vm-waveform-holder")

        imageProperty().bind(viewModel.waveformImageProperty)
    }
}
