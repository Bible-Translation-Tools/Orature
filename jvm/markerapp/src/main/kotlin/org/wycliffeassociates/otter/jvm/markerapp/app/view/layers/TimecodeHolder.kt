package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.glass.ui.Screen
import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow

class TimecodeHolder(
    viewModel: VerseMarkerViewModel,
    val height: Double,
    val imageWidth: Double = viewModel.imageWidth,
    durationMs: Int = viewModel.audioPlayer.getAbsoluteDurationMs()
) :
    ImageView(),
    ViewPortScrollable {

    val timecode: Timecode = Timecode(Math.floor(imageWidth), height)

    init {
        image = timecode.drawTimecode(durationMs)

        viewModel.positionProperty.onChangeAndDoNow {
            val x = it?.toDouble() ?: 0.0
            scrollTo(x - viewModel.padding)
        }
    }

    override fun scrollTo(x: Double) {
        val wd = Screen.getMainScreen().platformWidth
        viewport = Rectangle2D(x, 0.0, wd.toDouble(), height)
    }
}
