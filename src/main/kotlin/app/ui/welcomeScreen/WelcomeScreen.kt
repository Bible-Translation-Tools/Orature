package app.ui.welcomeScreen
import app.ui.styles.LayoutStyles
import app.ui.styles.ButtonStyles
import app.ui.styles.ButtonStyles.Companion.roundButtonMini
import app.ui.styles.LayoutStyles.Companion.userListContainer
import app.ui.styles.LayoutStyles.Companion.userListContainerBottom
import app.ui.styles.LayoutStyles.Companion.welcomeBackContainer
import app.ui.styles.LayoutStyles.Companion.windowView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.Priority
import tornadofx.*
import app.widgets.usersList.UsersList
import app.widgets.welcomeBack.WelcomeBack
import app.ui.userCreation.*
import java.io.File

class WelcomeScreen: View("Welcome Screen") {
    private val rad = 100.0
    val pad = 40.0
    val gridWidth = 400.0
//    val model = UserModel(Users())
    //WelcomeScreen splits the screen evenly with 2 subviews.
    //first subview shows an image of most recent logged in user, greetings, and home button
    //second subview shows a list of users created in the device, their own home buttons, and a button to create a new user
    private val welcomeScreen = hbox {
        var recentUser: File? = null
        var profileImages = mutableListOf<File>()
        val addUserIcon = MaterialIconView(MaterialIcon.GROUP_ADD, "25px")
        importStylesheet(ButtonStyles::class)
        importStylesheet(LayoutStyles:: class)
        addClass(windowView)
        vbox {
            style {
                addClass(welcomeBackContainer)
                hgrow = Priority.SOMETIMES
                vgrow = Priority.ALWAYS
            }
            recentUser?.let { add(WelcomeBack(recentUser)) }
        }
        vbox {
            style {
                addClass(userListContainer)
                hgrow = Priority.SOMETIMES
            }
            if (profileImages.isNotEmpty()) add(UsersList(profileImages))
            hbox {
                style {
                    addClass(userListContainerBottom)
                    vgrow = Priority.ALWAYS
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

