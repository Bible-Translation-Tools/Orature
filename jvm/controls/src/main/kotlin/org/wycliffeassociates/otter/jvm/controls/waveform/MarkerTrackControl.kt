package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.MarkerHighlightState
import org.wycliffeassociates.otter.jvm.controls.model.VerseMarkerModel
import org.wycliffeassociates.otter.jvm.controls.skins.waveform.MarkerTrackControlSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.observableListOf

class MarkerTrackControl() : Control() {

    val markers = observableListOf<ChunkMarkerModel>()
    val highlightState = observableListOf<MarkerHighlightState>()

    fun refreshMarkers() {
        (skin as? MarkerTrackControlSkin)?.let { it.refreshMarkers() }
    }

    override fun createDefaultSkin(): Skin<*> {
        return MarkerTrackControlSkin(this)
    }
}
