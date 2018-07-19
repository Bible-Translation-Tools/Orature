package app.ui.styles

import javafx.scene.Cursor
import tornadofx.*

class Styles: Stylesheet() {

    companion object {
        val rectangleButtonDefault by cssclass()
        val rectangleButtonAlternate by cssclass()
        val circleButton by cssclass()
    }

    init {

        rectangleButtonDefault {
            fill = c("#CC4141")
            backgroundColor+= c("#FFFF")
            minWidth = 100.0.px
            cursor= Cursor.HAND

        }

        rectangleButtonAlternate {
            fill = c("#FFFF")
            backgroundColor+= c("#CC4141")
            minWidth = 100.0.px
            cursor = Cursor.HAND

        }

        circleButton {
            fill = c("#CC4141")
            backgroundColor+= c("#FFFF")
            cursor = Cursor.HAND
        }

    }



}