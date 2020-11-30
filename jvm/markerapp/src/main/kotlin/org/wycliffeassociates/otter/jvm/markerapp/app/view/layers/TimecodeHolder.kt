package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.glass.ui.Screen
import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow

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
