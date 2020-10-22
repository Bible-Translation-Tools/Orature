package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.glass.ui.Screen
import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import org.wycliffeassociates.otter.jvm.markerapp.app.view.Timecode

class TimecodeHolder(
    val imageWidth: Double,
    val height: Double,
    durationMs: Int
):
    ImageView(),
    ViewPortScrollable {

    val timecode: Timecode

    init {
        timecode = Timecode(Math.floor(imageWidth), height)
        image = timecode.drawTimecode(durationMs)
    }

    override fun scrollTo(x: Double) {
        val wd = Screen.getMainScreen().platformWidth
        viewport = Rectangle2D(x, 0.0, wd.toDouble(), height)
    }
}
