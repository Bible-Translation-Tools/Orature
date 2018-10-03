package org.wycliffeassociates.otter.jvm.app.ui.welcomescreen

import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.Priority
import tornadofx.*
import org.wycliffeassociates.otter.jvm.app.widgets.userslist.UsersList
import org.wycliffeassociates.otter.jvm.app.ui.usercreation.*
import org.wycliffeassociates.otter.jvm.app.widgets.WidgetsStyles
import org.wycliffeassociates.otter.jvm.app.ui.welcomescreen.view.WelcomeScreenStyles
import java.io.File
import org.wycliffeassociates.otter.jvm.app.ui.imageLoader


class WelcomeScreen : View() {

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
    private val welcomeScreen = hbox {
        importStylesheet(WelcomeScreenStyles::class)
        importStylesheet(WidgetsStyles::class)
        var recentUser: File? = null
        var profileImages = mutableListOf<File>()
        addClass(WelcomeScreenStyles.windowView)
        vbox {
            addClass(WelcomeScreenStyles.welcomeBack)
            recentUser?.let {
                add(stackpane {
                    circle {
                        radius = 120.0
                        fill = c(Colors["baseBackground"])
                    }
                    button(graphic = imageLoader(recentUser)) {
                        addClass(WidgetsStyles.roundButtonLarge)
                    graphic.scaleX = 1.9
                    graphic.scaleY = 1.9
                    }
                })
                label(FX.messages["welcome"]).addClass(WelcomeScreenStyles.welcomeLabel)
                val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
                button("", homeIcon) {
                    importStylesheet(WidgetsStyles::class)
                    addClass(WidgetsStyles.alternateRectangleButton)
                    style {
                        minWidth = 175.0.px
                        homeIcon.fill = c(Colors["base"])
                    }
                }
            }
        }
        vbox {
            if (profileImages.isNotEmpty()) add(UsersList(profileImages))
            style {
                prefWidth = 1200.px
                vgrow = Priority.ALWAYS
                hgrow = Priority.ALWAYS
            }
            vbox(8) {
                addClass(WelcomeScreenStyles.createVBox).style { vgrow = Priority.ALWAYS }
                //INSIDE a vbox to allow for alignment
                val addUserIcon = MaterialIconView(MaterialIcon.GROUP_ADD, "25px")
                addUserIcon.fill = c(Colors["primary"])
                button("", addUserIcon) {
                        importStylesheet(WidgetsStyles::class)
                        addClass(WidgetsStyles.roundButton)
                    action {
                        find(WelcomeScreen::class).replaceWith(UserCreation::class)
                    }
                }
                padding = insets(pad)
                label(messages["create"]).addClass(WelcomeScreenStyles.createLabel)
            }
        }
    }
    //set the root of the view to the welcomescreen
    override val root = welcomeScreen

    //DON'T MOVE THIS TO THE TOP
    //current window will be null unless init goes under setting of root
    init {
        //set minimum size of window so they can always see the last user and the grid of other users
        val minWidth = 3 * pad + 2 * rad + gridWidth
        //add 100 for home button and Welcome message; probably in real thing these will be vars
        val minHeight = 2 * pad + 2 * rad + 100.0
        setWindowMinSize(minWidth, minHeight)
    }
}