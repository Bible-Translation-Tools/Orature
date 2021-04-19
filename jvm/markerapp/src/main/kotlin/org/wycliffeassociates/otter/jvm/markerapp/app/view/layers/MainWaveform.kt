package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.scene.image.ImageView
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel

private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#F7FAFF"

class MainWaveform(val viewModel: VerseMarkerViewModel) : ImageView() {

    init {
        styleClass.add("vm-waveform-holder")

        imageProperty().bind(viewModel.waveformImageProperty)
    }
}
