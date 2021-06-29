/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.jfoenix.controls.JFXButton
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class PlaceMarkerLayer(viewModel: VerseMarkerViewModel) : VBox() {
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
                        "vm-play-controls__btn--rounded",
                        "vm-play-controls__add-marker-btn"
                    )
                    setOnAction {
                        viewModel.placeMarker()
                    }
                }
            )
            style {
                styleClass.addAll("vm-play-controls__add-marker-container")
            }
        }
    }
}
