/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.waveform

import com.jfoenix.controls.JFXButton
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class PlaceMarkerLayer : VBox() {

    val onPlaceMarkerActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    fun onPlaceMarkerAction(op: () -> Unit) {
        onPlaceMarkerActionProperty.set(EventHandler { op.invoke() })
    }

    init {
        with(this) {
            isPickOnBounds = false

            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            alignment = Pos.BOTTOM_CENTER

            add(
                JFXButton("", FontIcon("mdi-bookmark-plus-outline")).apply {
                    styleClass.addAll(
                        "btn--cta",
                        "scrolling-waveform-play-controls__btn--rounded",
                        "scrolling-waveform-play-controls__add-marker-btn"
                    )
                    onActionProperty().bind(onPlaceMarkerActionProperty)
                }
            )
            style {
                styleClass.addAll("scrolling-waveform-play-controls__add-marker-container")
            }
        }
    }
}
