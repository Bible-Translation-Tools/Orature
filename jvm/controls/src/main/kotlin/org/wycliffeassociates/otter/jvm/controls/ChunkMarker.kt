package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.ChunkMarkerSkin
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.onChange
import tornadofx.plus
import kotlin.math.max

class ChunkMarker : Control() {

    val dragStartedX = SimpleDoubleProperty(0.0)
    val markerIdProperty = SimpleIntegerProperty(0)
    val draggedProperty = SimpleDoubleProperty(0.0)
    val markerPositionProperty = SimpleDoubleProperty(0.0)
    val markerNumberProperty = SimpleStringProperty("1")
    val isPlacedProperty = SimpleBooleanProperty(true)
    val canBeMovedProperty = SimpleBooleanProperty(true)

    init {
        setOnDragDetected {
            startFullDrag() // needed for the track to pick up the released event
            dragStartedX.set(it.screenX - layoutX)
        }

        setOnMouseDragged {
            val delta = it.screenX - dragStartedX.value
            draggedProperty.set(delta)
            translateX = max(markerPositionProperty.value + delta, 0.0)
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return ChunkMarkerSkin(this)
    }
}
