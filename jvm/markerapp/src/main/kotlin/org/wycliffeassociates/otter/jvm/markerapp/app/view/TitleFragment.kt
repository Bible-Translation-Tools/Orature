package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import tornadofx.*

class TitleFragment: Fragment() {
    override val root = vbox {
        alignment = Pos.CENTER
        text("Genesis Chapter 03") {
            style {
                fontSize = 1.em
            }
        }
        text("Add Verse Markers") {
            style {
                fontSize = 2.em
            }
        }
        style {
            padding = box(16.px)
            borderColor += box(Paint.valueOf("#00000020"))
            borderStyle += BorderStrokeStyle.SOLID
            borderWidth += box(1.px)
        }
    }
}