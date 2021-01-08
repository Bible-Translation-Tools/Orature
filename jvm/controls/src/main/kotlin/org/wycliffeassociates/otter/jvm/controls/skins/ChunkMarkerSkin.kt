package org.wycliffeassociates.otter.jvm.controls.skins

import javafx.scene.Cursor
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
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

        importStylesheet(javaClass.getResource("/css/chunk-marker.css").toExternalForm())

        children.add(
            HBox().apply {

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

                styleClass.add("chunk-marker")
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
        )
    }
}
