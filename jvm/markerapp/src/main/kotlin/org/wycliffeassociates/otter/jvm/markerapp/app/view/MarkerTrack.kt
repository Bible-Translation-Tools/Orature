package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.collections.FXCollections
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
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

        val markers = FXCollections.observableArrayList<ChunkMarker>()
        val rectangles = FXCollections.observableArrayList<Rectangle>()

        viewModel.markers.markerCountProperty.onChangeAndDoNow {
            markers.clear()
            markers.setAll(
                viewModel.markers.cues.mapIndexed { index, cue ->
                    if(index > 0) {
                        val rectWidth = (cue.location - viewModel.markers.cues[index-1].location) / scale
                        rectangles.add(Rectangle(rectWidth, height).apply {
                            xProperty().set(viewModel.markers.cues[index-1].location / scale.toDouble())
                            fill = if(index % 2 == 0) { Paint.valueOf("#1edd7633") } else { Paint.valueOf("#015ad933")}
                        })
                    } else {
                        val rectWidth = (viewModel.audioPlayer.getAbsoluteDurationInFrames() - cue.location) / scale
                        rectangles.add(Rectangle(rectWidth, height).apply {
                            xProperty().set(viewModel.audioPlayer.getAbsoluteDurationInFrames() - cue.location / scale.toDouble())
                            fill = if(index % 2 == 0) { Paint.valueOf("#1edd7633") } else { Paint.valueOf("#015ad933")}
                        })
                    }
                    ChunkMarker().apply {
                        markerNumberProperty.set(cue.label)
                        val x = cue.location / scale.toDouble()
                        translateXProperty().set(x)
                    }
                }
            )
        }

        markers.onChangeAndDoNow {
            children.clear()
            children.addAll(rectangles)
            children.addAll(markers)
        }
    }
}
