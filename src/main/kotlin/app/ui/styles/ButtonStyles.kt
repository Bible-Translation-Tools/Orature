package app.ui.styles

import app.UIColorsObject.Colors
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import tornadofx.*

class ButtonStyles : Stylesheet() {

    companion object {
        val rectangleButtonDefault by cssclass()
        val rectangleButtonAlternate by cssclass()
        val circleButton by cssclass()
        val roundButton by cssclass()
    }

    init {

        rectangleButtonDefault {
            fill = c(Colors["accent"])
            backgroundColor += c(Colors["base"])
            minWidth = 100.0.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, c(Colors["dropShadow"]))

            and(hover) {
                scaleX = 1.1
                scaleY = 1.1
            }
        }

        rectangleButtonAlternate {
            fill = c(Colors["base"])
            backgroundColor += c(Colors["accent"])
            minWidth = 100.0.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, c(Colors["dropShadow"]))

            and(hover) {
                scaleX = 1.1
                scaleY = 1.1
            }

        }

        circleButton {
            fill = c(Colors["accent"])
            backgroundColor += c(Colors["base"])
            cursor = Cursor.HAND
        }

        roundButton {
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            effect = DropShadow(10.0,  c(Colors["dropShadow"]))
            minWidth = 64.0.px
            minHeight = 64.0.px
            cursor = Cursor.HAND
            backgroundColor +=  c(Colors["base"])
            borderColor += box(c(Colors["transparent"]))

            and(hover) {
                scaleX = 1.1
                scaleY = 1.1
            }
        }

    }
}

