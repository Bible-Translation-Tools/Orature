package org.wycliffeassociates.otter.jvm.controls.marker

import org.wycliffeassociates.otter.common.domain.model.ChunkMarkerModel
import tornadofx.*

class MarkersContainer: MarkerTrackControl() {
    override fun createMarker(): MarkerControl {
        return MarkerNode().apply {
            prefHeightProperty().bind(this@MarkersContainer.heightProperty())
        }
    }

    /**
     * Modifies the creation of marker rectangles to cover the waveform area
     * starting from the top track with n pixels vertical offset (translateY).
     */
    override fun preallocateMarkers() {
        for (i in 0 until MARKER_COUNT) {
            val mk = ChunkMarkerModel(0, i.toString(), false)
            val marker = createMarker(i, mk)
            val rect = createHighlight(i, mk)
            rect.heightProperty().bind(heightProperty().minus(80))
            rect.translateXProperty().bind(marker.translateXProperty())
            rect.translateY = 80.0
            rect.visibleProperty().bind(marker.visibleProperty())

            _markers.add(marker)
            highlights.add(rect)
        }
    }
}
