package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.ChunkMarkerSkin
import tornadofx.minus

class ChunkMarker: Control() {

    val draggedProperty = SimpleDoubleProperty(0.0)
    val markerNumberProperty = SimpleStringProperty("1")
    val isPlacedProperty = SimpleBooleanProperty(true)
    val canBeMovedProperty = SimpleBooleanProperty(true)

    init {
        layoutXProperty().bind(draggedProperty)
    }

    override fun createDefaultSkin(): Skin<*> {
        return ChunkMarkerSkin(this)
    }
}
