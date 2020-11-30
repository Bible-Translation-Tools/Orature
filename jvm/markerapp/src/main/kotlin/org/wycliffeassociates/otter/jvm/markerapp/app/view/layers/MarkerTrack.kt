package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.glass.ui.Screen
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

        widthProperty().onChange {
            scale = viewModel.audioPlayer.getAbsoluteDurationInFrames() / it
            resetMakers()
        }

        viewModel.markers.markerCountProperty.onChangeAndDoNow {
            resetMakers()
        }

        markers.onChangeAndDoNow {
            children.clear()
            children.addAll(markers)
        }
    }

    private fun resetMakers() {
        markers.clear()
        markers.setAll(
            viewModel.markers.cues.mapIndexed { index, cue ->
                ChunkMarker().apply {
                    markerNumberProperty.set(cue.label)
                    val x = cue.location / scale.toDouble()
                    translateXProperty().set(x)
                }
            }
        )
    }
}
