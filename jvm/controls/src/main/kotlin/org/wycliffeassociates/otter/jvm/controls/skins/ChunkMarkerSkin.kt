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
package org.wycliffeassociates.otter.jvm.controls.skins

import javafx.scene.Cursor
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import tornadofx.*

class ChunkMarkerSkin(val control: ChunkMarker) : SkinBase<ChunkMarker>(control) {

    val dragIcon = FontIcon("gmi-drag-handle")
    val placedBookmarkIcon = FontIcon("mdi-bookmark")
    val addBookmarkIcon = FontIcon("mdi-bookmark-plus-outline")

    init {
        dragIcon.visibleProperty().bind(control.canBeMovedProperty)
        dragIcon.managedProperty().bind(control.canBeMovedProperty)
        placedBookmarkIcon.visibleProperty().bind(control.isPlacedProperty)
        placedBookmarkIcon.managedProperty().bind(control.isPlacedProperty)
        addBookmarkIcon.visibleProperty().bind(placedBookmarkIcon.visibleProperty().not())
        addBookmarkIcon.managedProperty().bind(placedBookmarkIcon.managedProperty().not())

        children.add(
            HBox().apply {
                styleClass.add("chunk-marker__root")
                var priorCursor = Cursor.DEFAULT
                var dragging = false

                setOnMouseEntered {
                    if (skinnable.canBeMovedProperty.value) {
                        priorCursor = Cursor.OPEN_HAND
                        if (!dragging) {
                            cursor = Cursor.OPEN_HAND
                        }
                    }
                }

                setOnMouseExited {
                    priorCursor = Cursor.DEFAULT
                    if (!dragging) {
                        cursor = Cursor.DEFAULT
                    }
                }

                setOnMousePressed {
                    if (skinnable.canBeMovedProperty.value) {
                        dragging = true
                        cursor = Cursor.CLOSED_HAND
                    }
                }

                setOnMouseReleased {
                    dragging = false
                    cursor = priorCursor
                }

                hbox {
                    hgrow = Priority.ALWAYS
                    addClass("chunk-marker__container")

                    add(dragIcon)
                    add(placedBookmarkIcon)
                    add(addBookmarkIcon)
                    add(
                        text {
                            styleClass.add("chunk-marker__text")
                            textProperty().bind(control.markerNumberProperty)
                        }
                    )
                }
            }
        )
    }
}
