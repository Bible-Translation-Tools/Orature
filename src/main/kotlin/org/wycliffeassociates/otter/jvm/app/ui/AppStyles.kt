package org.wycliffeassociates.otter.jvm.app.theme

import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import tornadofx.*

class AppStyles : Stylesheet() {

    companion object {
        val backButton by cssclass()
    }

    init {
        // Material design scroll bar
        scrollBar {
            backgroundColor += Color.TRANSPARENT
            padding = box(0.px, 4.px)
            prefWidth = 16.px
            Stylesheet.thumb {
                backgroundColor += Color.DARKGRAY
                backgroundRadius += box(10.px)
            }
            Stylesheet.incrementArrow {
                visibility = FXVisibility.COLLAPSE
            }
            Stylesheet.decrementArrow {
                visibility = FXVisibility.COLLAPSE
            }
        }

        scrollPane {
            backgroundColor += Color.TRANSPARENT
        }

        button {
            and(backButton) {
                minWidth = 230.px
                textFill = Color.WHITE
                child("*") {
                    fill = Color.WHITE
                }
                backgroundColor += c(UIColorsObject.Colors["primary"])
                unsafe("-jfx-button-type", raw("RAISED"))
            }
        }

        // Load the fonts
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Regular.ttf"), 10.0)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Bold.ttf"), 10.0)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-BoldItalic.ttf"), 10.0)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Italic.ttf"), 10.0)
        root {
            font = Font.font("Noto Sans", 10.0)
            fontWeight = FontWeight.NORMAL
        }
    }
}
