package app.ui.styles

import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*

class LayoutStyles: Stylesheet() {

        companion object {
            val windowView by cssclass()
        }

        init {
            windowView {
                prefHeight = 700.px
                prefWidth = 1100.px
            }
        }
    }

