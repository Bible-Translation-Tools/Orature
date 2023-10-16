package org.wycliffeassociates.otter.jvm.controls.marker

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.input.MouseEvent

abstract class MarkerControl : Control() {
    open val markerIdProperty = SimpleIntegerProperty(0)
    open val markerPositionProperty = SimpleDoubleProperty(0.0)
    open val markerNumberProperty: StringProperty = SimpleStringProperty()
    open val isPlacedProperty = SimpleBooleanProperty(true)
    open val canBeMovedProperty = SimpleBooleanProperty(true)

    open val onDragStart: ObjectProperty<EventHandler<MouseEvent>> = SimpleObjectProperty()
    open val onDragProperty: ObjectProperty<EventHandler<MouseEvent>> = SimpleObjectProperty()

    fun setOnDragStart(eventHandler: EventHandler<MouseEvent>?) {
        onDragStart.set(eventHandler)
    }

    fun setOnDrag(eventHandler: EventHandler<MouseEvent>?) {
        onDragProperty.set(eventHandler)
    }
}