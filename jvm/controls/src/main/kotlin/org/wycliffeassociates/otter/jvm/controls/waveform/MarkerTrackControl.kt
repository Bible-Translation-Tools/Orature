package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.MarkerHighlightState
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.MarkerTrackControlSkin
import tornadofx.observableListOf

class MarkerTrackControl : Control() {

    val markers = observableListOf<ChunkMarkerModel>()
    val highlightState = observableListOf<MarkerHighlightState>()

    fun refreshMarkers() {
        (skin as? MarkerTrackControlSkin)?.let { it.refreshMarkers() }
    }

    override fun createDefaultSkin(): Skin<*> {
        return MarkerTrackControlSkin(this)
    }
}
