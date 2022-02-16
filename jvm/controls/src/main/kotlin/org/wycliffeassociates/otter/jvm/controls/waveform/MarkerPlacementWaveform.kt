package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.MarkerPlacementWaveformSkin

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
