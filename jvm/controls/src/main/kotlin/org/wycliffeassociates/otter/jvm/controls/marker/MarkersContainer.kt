package org.wycliffeassociates.otter.jvm.controls.marker

class MarkersContainer: MarkerTrackControl() {
    override fun createMarker(): MarkerControl {
        return MarkerNode().apply {
            prefHeightProperty().bind(this@MarkersContainer.heightProperty())
        }
    }
}
