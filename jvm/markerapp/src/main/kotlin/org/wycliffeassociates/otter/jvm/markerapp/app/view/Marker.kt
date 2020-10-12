package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class Marker(number: String) : HBox() {
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
        val icon = FontIcon("mdi-bookmark")

        icon.iconColor = Color.WHITE
        icon.iconSize = 22
        add(icon)
        text(number) {
            fill = Color.WHITE
            style {
                fontSize = 16.px
            }
        }

    }
}
