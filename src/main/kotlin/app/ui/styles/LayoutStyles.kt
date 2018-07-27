package app.ui.styles

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import tornadofx.*

class LayoutStyles: Stylesheet() {

        companion object {
            val windowView by cssclass()
            val welcomeBackContainer by cssclass()
            val userListContainer by cssclass()
            val userListContainerBottom by cssclass()
        }

        init {
            windowView {
                prefHeight = 700.px
                prefWidth = 1100.px
            }
            welcomeBackContainer {
                alignment = Pos.CENTER
                prefWidth = 500.px
                backgroundColor += Color.WHITE
            }
            userListContainer {
                prefWidth = 500.px
                padding = tornadofx.box(50.px)
                backgroundColor += c("#DFDEE3")
            }
            userListContainerBottom {
                alignment = Pos.BOTTOM_RIGHT
                minHeight = 70.px
            }
        }
    }

