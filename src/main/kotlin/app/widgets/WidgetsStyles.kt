package app.widgets

import app.UIColorsObject
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*


class WidgetsStyles : Stylesheet() {
    companion object {
        val alternateRectangleButton by cssclass()
        val rectangleButtonDefault by cssclass()
        val ProfileIcon by cssclass()
        val UsersListGrid by cssclass()
    }

    init {
        rectangleButtonDefault {
            fill = c("#CC4141")
            backgroundColor += c("#FFF")
            minWidth = 100.0.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, c(UIColorsObject.Colors["baseBackground"]))

            and(hover) {
                scaleX = 1.1
                scaleY = 1.1
            }
        }

        alternateRectangleButton {
            fill = c("#FFF")
            backgroundColor += c("#CC4141")
            minWidth = 100.0.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, Color.GRAY)

            and(hover) {
                scaleX = 1.1
                scaleY = 1.1
            }
        }

        ProfileIcon {
            backgroundColor += c(UIColorsObject.Colors["base"])
            backgroundRadius += box(100.percent)
            borderRadius += box(100.percent)
            cursor = Cursor.HAND
            effect = DropShadow(10.0, Color.GRAY)
            cursor = Cursor.HAND

            and(hover) {
                opacity = 0.90
                scaleX = 1.1
                scaleY = 1.1
            }
        }

        UsersListGrid{
            verticalCellSpacing = 25.0.px
            backgroundColor += c(UIColorsObject.Colors["baseMedium"])
            prefHeight = 800.0.px
            verticalCellSpacing = 24.0.px
            maxCellsInRow = 3
            horizontalCellSpacing = 32.0.px
        }
    }
}