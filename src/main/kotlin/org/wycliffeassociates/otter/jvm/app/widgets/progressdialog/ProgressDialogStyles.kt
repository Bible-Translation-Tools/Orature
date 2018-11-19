package org.wycliffeassociates.otter.jvm.app.widgets

import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.ContentDisplay
import javafx.scene.effect.DropShadow
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import tornadofx.*


class ProgressDialogStyles : Stylesheet() {
    companion object {
        val default by cssclass()
    }

    init {
        default {
            // Graphic
            child("*") {
                fill = c(Colors["baseText"])
            }
            backgroundColor += c(Colors["base"])
            progressIndicator {
                progressColor = c(Colors["baseText"])
                maxWidth = 125.px
                maxHeight = 125.px
            }
            prefWidth = 500.px
            prefHeight = 300.px

            label {
                fontWeight = FontWeight.BOLD
                fontSize = 16.px
                padding = box(20.px, 20.px)
                textAlignment = TextAlignment.CENTER
                fillWidth = true
                maxWidth = Double.MAX_VALUE.px
                alignment = Pos.CENTER
                wrapText = true
            }
        }

    }
}