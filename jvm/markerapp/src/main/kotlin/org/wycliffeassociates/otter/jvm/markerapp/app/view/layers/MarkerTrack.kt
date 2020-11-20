package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.collections.FXCollections
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class MarkerTrack(val viewModel: VerseMarkerViewModel) : Region() {

    var scale: Double = 1.0
    private val markers = FXCollections.observableArrayList<ChunkMarker>()

    init {
        styleClass.add("vm-marker-track")

        for (i in 1 until viewModel.markers.markerTotal + 1) {
            markers.add(
                ChunkMarker().apply {
                    markerNumberProperty.set((i).toString())
                    if (i == 1) {
                        canBeMovedProperty.set(false)
                    } else {
                        visibleProperty().set(false)
                    }
                }
            )
        }

        widthProperty().onChange {
            scale = viewModel.audioPlayer.getAbsoluteDurationInFrames() / it
            resetMakers()
        }

        viewModel.markers.markerCountProperty.onChangeAndDoNow {
            resetMakers()
        }

        children.addAll(markers)
    }

    private fun resetMakers() {
        viewModel.markers.cues.mapIndexed { index, cue ->
            val marker = markers.get(index)
            marker.markerNumberProperty.set(cue.label)
            val x = cue.location / scale.toDouble()
            marker.translateXProperty().set(x)
            marker.visibleProperty().set(true)
        }
    }
}
