package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.collections.FXCollections
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import org.wycliffeassociates.otter.jvm.markerapp.app.view.pixelsToFrames
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class MarkerTrack(val viewModel: VerseMarkerViewModel) : Region() {

    var scale: Double = 1.0
    private val markerViewList = FXCollections.observableArrayList<ChunkMarker>()

    init {
        styleClass.add("vm-marker-track")

        for (i in 1 until viewModel.markers.markerTotal + 1) {
            markerViewList.add(
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

        children.addAll(markerViewList)
    }

    private fun resetMakers() {
        viewModel.markers.markers.map { cue ->
            markerViewList.find { it.markerIdProperty.value == cue.id }?.let { marker ->
                marker.markerNumberProperty.set(cue.label)
                val x = cue.frame / scale.toDouble()
                marker.translateXProperty().set(x)
                marker.markerPositionProperty.set(x)
                marker.visibleProperty().set(true)
            }
        }
    }
}
