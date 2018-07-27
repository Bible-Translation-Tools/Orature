package app.ui.styles

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class LayoutStyles: Stylesheet() {

        companion object {
            val windowView by cssclass()
            val welcomeBackContainer by cssclass()
            val userListContainer by cssclass()
            val userListContainerBottom by cssclass()
            val mostRecentUserContainer by cssclass()
            val welcomeBackText by cssclass()
            val usersListGrid by cssclass()
            val usersListCell by cssclass()
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
            mostRecentUserContainer {
                alignment = Pos.CENTER
                spacing = 15.px
            }
            welcomeBackText {
                fontSize = 32.0.px
                FontWeight.BOLD
            }
            usersListGrid {
                prefHeight = 900.0.px
                cellHeight = 170.0.px
                verticalCellSpacing = 15.0.px
                horizontalCellSpacing = 20.0.px
                backgroundColor += Color.valueOf("#DFDEE3")
            }
            usersListCell {
                backgroundColor += Color.valueOf("#DFDEE3")
                alignment = Pos.CENTER
            }
        }
    }

