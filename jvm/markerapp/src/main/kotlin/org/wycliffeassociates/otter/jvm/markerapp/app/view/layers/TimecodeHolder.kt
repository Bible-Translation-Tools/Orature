package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.scene.image.ImageView
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel

class TimecodeHolder(
    viewModel: VerseMarkerViewModel,
    val height: Double,
    val imageWidth: Double = viewModel.imageWidth,
    durationMs: Int = viewModel.audioPlayer.getAbsoluteDurationMs()
) : ImageView() {

    val timecode: Timecode = Timecode(Math.floor(imageWidth), height)

    init {
        image = timecode.drawTimecode(durationMs)
    }
}
