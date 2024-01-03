/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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

/**
 * Provides a common interface for the old and new marker UI controls.
 */
abstract class MarkerControl : Control() {
    open val markerIdProperty = SimpleIntegerProperty(0)
    open val markerIndexProperty = SimpleIntegerProperty(0)
    open val markerPositionProperty = SimpleDoubleProperty(0.0)
    open val markerNumberProperty: StringProperty = SimpleStringProperty()
    open val isPlacedProperty = SimpleBooleanProperty(true)
    open val canBeMovedProperty = SimpleBooleanProperty(true)
    open val canBeDeletedProperty = SimpleBooleanProperty(true)

    /**
     * Delegates the drag start (on marker clicked) to whatever the actual drag control/button is.
     */
    open val onDragStartProperty: ObjectProperty<EventHandler<MouseEvent>> = SimpleObjectProperty()
    /**
     * Delegates the drag handler (on marker dragged) to whatever the actual drag control/button is.
     */
    open val onDragProperty: ObjectProperty<EventHandler<MouseEvent>> = SimpleObjectProperty()

    open val onDragFinishProperty: ObjectProperty<EventHandler<MouseEvent>> = SimpleObjectProperty()

    fun setOnDragStart(eventHandler: EventHandler<MouseEvent>?) {
        onDragStartProperty.set(eventHandler)
    }

    fun setOnDrag(eventHandler: EventHandler<MouseEvent>?) {
        onDragProperty.set(eventHandler)
    }

    fun setOnDragFinish(eventHandler: EventHandler<MouseEvent>?) {
        onDragFinishProperty.set(eventHandler)
    }
}