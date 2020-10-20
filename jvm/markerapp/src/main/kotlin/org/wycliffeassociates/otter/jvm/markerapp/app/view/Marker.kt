package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class Marker(number: String) : HBox() {

    val isPlacedProperty = SimpleBooleanProperty(true)
    val canBeMovedProperty = SimpleBooleanProperty(true)

    init {
        prefHeight = 40.0
        prefWidth = 60.0

        alignment = Pos.CENTER_LEFT

        style {
            spacing = 2.px
            padding = box(8.px)
            backgroundColor += Paint.valueOf("#06429b")
            borderColor += box(Paint.valueOf("#0a3373"))
            backgroundRadius += box(0.px, 4.px, 4.px, 0.px)
            borderRadius += box(0.px, 4.px, 4.px, 0.px)
        }
        val dragIcon = FontIcon("gmi-drag-handle")
        val placedBookmarkIcon = FontIcon("mdi-bookmark")
        val addBookmarkIcon = FontIcon("mdi-bookmark-plus-outline")

        dragIcon.iconColor = Color.WHITE
        dragIcon.iconSize = 22
        dragIcon.visibleProperty().bind(canBeMovedProperty)
        dragIcon.managedProperty().bind(canBeMovedProperty)

        placedBookmarkIcon.iconColor = Color.WHITE
        placedBookmarkIcon.iconSize = 22
        placedBookmarkIcon.visibleProperty().bind(isPlacedProperty)
        placedBookmarkIcon.managedProperty().bind(isPlacedProperty)

        addBookmarkIcon.iconColor = Color.WHITE
        addBookmarkIcon.iconSize = 22
        addBookmarkIcon.visibleProperty().bind(placedBookmarkIcon.visibleProperty().not())
        addBookmarkIcon.managedProperty().bind(placedBookmarkIcon.managedProperty().not())

        add(dragIcon)
        add(placedBookmarkIcon)
        add(addBookmarkIcon)

        text(number) {
            fill = Color.WHITE
            style {
                fontSize = 16.px
            }
        }
    }
}
