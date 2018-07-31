package app.widgets.welcomeBack

import app.ui.imageLoader
import app.ui.styles.ButtonStyles.Companion.roundButtonLarge
import app.ui.styles.LayoutStyles.Companion.mostRecentUserContainer
import app.ui.styles.LayoutStyles.Companion.welcomeBackText
import app.ui.styles.Styles.Companion.rectangleButtonAlternate
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File


class WelcomeBack(ImageFile: File) : HBox() {
    init {
        //sets component center horizontally
        alignment = Pos.CENTER
    }
    private val rad = 125.0
    val homeIcon = MaterialIconView(MaterialIcon.HOME,"25px")

    private val container = vbox {
        style {
            addClass(mostRecentUserContainer)
            vgrow = Priority.SOMETIMES
        }
        stackpane {
            //Outer circle
            circle(radius = 120.0) {
                fill = c("#E5E5E5")
            }
            //Big Profile Icon
            button(graphic = imageLoader(ImageFile)) {
                addClass(roundButtonLarge)
                graphic.scaleX = 1.9
                graphic.scaleY = 1.9
            }
        }
        label("Welcome Back!") {
            addClass(welcomeBackText)
        }
        button(graphic = homeIcon) {
            addClass(rectangleButtonAlternate)
            style {
                homeIcon.fill = c("#FFFFFF")
            }
        }

        //prevents from spreading out to take up whole screen when window maximized
        //note: 100 extra pixels hard coded in for space,
        // but we may change this val depending on size of home button and text
        setMaxSize(2 * rad, 3 * rad);
        setPrefSize(2 * rad + 100, 3 * rad)
        usePrefSize
    }
}

