package app.ui.welcomeScreen.View

import app.UIColorsObject
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*
import java.awt.Window

class WelcomeScreenStyles : Stylesheet() {

    companion object {
        val welcomeBack by cssclass()
        val welcomeLabel by cssclass()
        val createVBox by cssclass()
        val createLabel by cssclass()
        val userListContainer by cssclass()
        val windowView by cssclass()
        val userListContainerBottom by cssclass()
        val mostRecentUserContainer by cssclass()
    }

    init{
        welcomeBack{
            alignment = Pos.CENTER
            spacing = 34.0.px
            backgroundColor += c(UIColorsObject.Colors["base"])
            prefWidth = 1200.px
        }
        welcomeLabel{
            fontSize = 32.0.px
            FontWeight.BOLD
        }
        createVBox{
            backgroundColor += c(UIColorsObject.Colors["baseMedium"])
            prefHeight = 50.0.px
            alignment = Pos.BOTTOM_RIGHT
        }
        createLabel {
            fontWeight = FontWeight.BOLD
            padding = box(5.px)
        }
        windowView {
            prefHeight = 700.px
            prefWidth = 1100.px
        }
        userListContainer {
            prefWidth = 500.px
            padding = box(50.px)
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
    }
}