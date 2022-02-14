package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.waveform.ScrollingWaveform
import org.wycliffeassociates.otter.jvm.markerapp.app.model.VerseMarkerModel

class MarkerPlacementWaveform(
    val topNode: Node
) : ScrollingWaveform() {

    val markerStateProperty = SimpleObjectProperty<VerseMarkerModel>()

    var onSeekNext: () -> Unit = {}
    var onSeekPrevious: () -> Unit = {}
    var onPlaceMarker: () -> Unit = {}
    var topTrack: Node? = topNode
    var bottomTrack: Node? = null

    override fun createDefaultSkin(): Skin<*> {
        return MarkerPlacementWaveformSkin(this)
    }
}
