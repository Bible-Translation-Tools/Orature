package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.ChunkMarkerSkin

class ChunkMarker : Control() {

    val markerIdProperty = SimpleIntegerProperty(0)
    val markerPositionProperty = SimpleDoubleProperty(0.0)
    val markerNumberProperty = SimpleStringProperty("1")
    val isPlacedProperty = SimpleBooleanProperty(true)
    val canBeMovedProperty = SimpleBooleanProperty(true)

    init {
        visibleProperty().bind(isPlacedProperty)
    }

    override fun createDefaultSkin(): Skin<*> {
        return ChunkMarkerSkin(this)
    }
}
