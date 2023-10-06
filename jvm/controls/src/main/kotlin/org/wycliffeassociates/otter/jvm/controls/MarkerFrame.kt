package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.MouseEvent
import org.wycliffeassociates.otter.jvm.controls.skins.MarkerFrameSkin
import tornadofx.*

class MarkerFrame  : Control() {

    val markerIdProperty = SimpleIntegerProperty(0)
    val markerPositionProperty = SimpleDoubleProperty(0.0)
    val markerNumberProperty = SimpleStringProperty("1")
    val isPlacedProperty = SimpleBooleanProperty(true)
    val canBeMovedProperty = SimpleBooleanProperty(true)

    val onClickProperty: ObjectProperty<EventHandler<MouseEvent>> = SimpleObjectProperty()
    val onDragProperty: ObjectProperty<EventHandler<MouseEvent>> = SimpleObjectProperty()

    init {
        addClass("marker-frame")
        visibleProperty().bind(isPlacedProperty)
    }

    override fun createDefaultSkin(): Skin<*> {
        return MarkerFrameSkin(this)
    }

    fun setOnClick(eventHandler: EventHandler<MouseEvent>?) {
        onClickProperty.set(eventHandler)
    }

    fun setOnDrag(eventHandler: EventHandler<MouseEvent>?) {
        onDragProperty.set(eventHandler)
    }
}
