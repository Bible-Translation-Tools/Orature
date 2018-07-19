package app.ui.welcomeScreen
import app.ui.styles.ButtonStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color

import tornadofx.*;
import app.ui.widgets.usersList.UsersList
import app.ui.widgets.welcomeBack.WelcomeBack
import java.awt.Window
import app.ui.userCreation.*

class WelcomeScreen: View("Welcome Screen") {

    //grab the usernames from outside
    //in the real thing, we'll grab icons and sound clips instead
    //so replace this injection with whatever injection you do

    val gridWidth = 400.0

    //the left half of the screen, which displays:
    //the last user to log in, a welcome message, and a button to go to that user's home
    private val rad = 100.0

    //the grid of users
    //hooked up to the list we pulled in up top from DataService
    //right now it has just 9 elems but the real one will have who-knows-how-many

    val pad = 60.0
    private val welcomeScreen = hbox() {

        vbox {
            alignment = Pos.CENTER
            stackpane {
                add(WelcomeBack());
                style {
                    setMinHeight(Window.HEIGHT.toDouble())
                    backgroundColor += Color.WHITE
                    vgrow = Priority.ALWAYS
                    hgrow=Priority.ALWAYS
                    //setMinWidth(Screen.getPrimary().bounds.width/2)
                    prefWidth= 1200.px
                }
            }

        }

        vbox {
            add(UsersList())
            style {
                prefWidth=1200.px
                vgrow= Priority.ALWAYS
                hgrow= Priority.ALWAYS
            }

            hbox {
                val addUserIcon = MaterialIconView(MaterialIcon.GROUP_ADD, "25px")
                alignment = Pos.BOTTOM_RIGHT
                style {
                    backgroundColor += c("#DFDEE3")
                    vgrow = Priority.ALWAYS
                    prefHeight = 50.0.px

                }

                button("", addUserIcon) {
                    alignment = Pos.CENTER
                    style {
                        importStylesheet(ButtonStyles::class)
                        addClass(ButtonStyles.roundButton)
                        addUserIcon.fill = c("#CC4141")
                    }

                    action {
                        find(WelcomeScreen::class).replaceWith(UserCreation::class,transition = ViewTransition.Slide(.9.seconds))
                    }
                }
                padding = insets(pad);
           }

        }

        //make sure the plus button is in the bottom right
        //BorderPane.setAlignment(plusButton.root, Pos.BOTTOM_RIGHT);
        //bottom = plusButton;
        //put in some nice margins so it's not too crowded

    }

    //set the root of the view to the welcomeScreen
    override val root = welcomeScreen;

    //DON'T MOVE THIS TO THE TOP
    //current window will be null unless init goes under setting of root
    init{
        //set minimum size of window so they can always see the last user and the grid of other users
        val minWidth = 3 * pad + 2 * rad + gridWidth;
        //add 100 for home button and Welcome message; probably in real thing these will be vars
        val minHeight = 2 * pad + 2 * rad + 100.0;
        setWindowMinSize(minWidth, minHeight);
    }
}

