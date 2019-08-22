package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.geometry.Pos
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class InfoItem(major: String, minor: String? = null, bold: Boolean = true) : HBox() {
    init {
        alignment = Pos.CENTER
        style {
            padding = box(0.px, 0.px, 0.px, 20.px)
        }

        text(major) {
            fill = Color.WHITE
            style {
                fontWeight = if (bold) FontWeight.BOLD else FontWeight.NORMAL
                fontSize = 24.pt
                fontFamily = "noto sans"
            }
        }
        minor?.let {
            region {
                style {
                    padding = box(0.px, 0.px, 0.px, 10.px)
                }
            }
            label(it) {
                minWidth = 50.0
                alignment = Pos.CENTER
                background = Background(BackgroundFill(Color.valueOf("222222"), null, null))

                style {
                    padding = box(2.px, 5.px, 2.px, 5.px)
                    fontSize = 20.pt
                    fontFamily = "noto sans"
                    alignment = Pos.CENTER
                    textFill = Color.WHITE
                }
            }
        }
    }
}

