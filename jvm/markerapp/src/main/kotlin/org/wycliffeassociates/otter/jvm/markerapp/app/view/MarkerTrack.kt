package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.collections.FXCollections
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class MarkerTrack(viewModel: VerseMarkerViewModel, width: Double, height: Double) : Region() {
    init {
        style {
            backgroundColor += Paint.valueOf("#c8d2e3")
        }

        val scale = viewModel.audioPlayer.getAbsoluteDurationInFrames() / width

        prefWidthProperty().set(width)
        prefHeightProperty().set(height)

        val markers = FXCollections.observableArrayList<Marker>()
        viewModel.markers.markerCountProperty.onChangeAndDoNow {
            markers.clear()
            markers.setAll(
                viewModel.markers.cues.map {
                    Marker(it.label).apply {
                        val x = it.location / scale.toDouble()
                        translateXProperty().set(x)
                    }
                }
            )
        }

        markers.onChangeAndDoNow {
            children.clear()
            children.addAll(markers)
        }
    }
}
