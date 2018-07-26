package app.ui.welcomeScreen
import app.ui.imageLoader
import app.ui.styles.ButtonStyles
import app.ui.styles.ButtonStyles.Companion.roundButtonMini
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color

import tornadofx.*
import app.widgets.usersList.UsersList
import app.widgets.welcomeBack.WelcomeBack
import app.ui.userCreation.*
import java.io.File

class WelcomeScreen: View("Welcome Screen") {

    private val rad = 100.0
    val pad = 40.0
    val gridWidth = 400.0

//    WelcomeScreen splits the screen evenly with 2 subviews.
//    first subview shows an image of most recent logged in user, greetings, and home button
//    second subview shows a list of users created in the device, their own home buttons, and a button to create a new user
    private val welcomeScreen = hbox {

        var data1: File? = null
        var profileImages = mutableListOf<File>()
        val addUserIcon = MaterialIconView(MaterialIcon.GROUP_ADD, "25px")

        importStylesheet(ButtonStyles::class)
        style {
            prefHeight = 700.px
        }

        vbox {

            style {
                prefWidth = 500.px
                hgrow = Priority.ALWAYS
            }

            stackpane {
                if ( data1 !== null ) add( WelcomeBack(data1) )

                style {
                    backgroundColor += Color.WHITE
                    vgrow = Priority.ALWAYS
                }

            }
        }

        vbox {

            style {
                prefWidth = 500.px
                hgrow = Priority.ALWAYS
                padding = box(pad.px)
                backgroundColor += c("#DFDEE3")

            }

            if (profileImages.isNotEmpty()) {
                add(UsersList(profileImages))
            }

            hbox {

                style {
                    vgrow = Priority.ALWAYS
                    alignment = Pos.BOTTOM_RIGHT
                    minHeight = 70.px
                }

                button(graphic = addUserIcon) {

                    style {
                        addClass(roundButtonMini)
                        addUserIcon.fill = c("#CC4141")
                    }

                    action {
                        find(WelcomeScreen::class).replaceWith(UserCreation::class)
                    }
                }
           }

        }
    }

    //set the root of the view to the welcomeScreen
    override val root = welcomeScreen

    //DON'T MOVE THIS TO THE TOP
    //current window will be null unless init goes under setting of root
    init{
        //set minimum size of window so they can always see the last user and the grid of other users
        val minWidth = 3 * pad + 2 * rad + gridWidth
        //add 100 for home button and Welcome message; probably in real thing these will be vars
        val minHeight = 2 * pad + 2 * rad + 100.0
        setWindowMinSize(minWidth, minHeight)
    }
}

