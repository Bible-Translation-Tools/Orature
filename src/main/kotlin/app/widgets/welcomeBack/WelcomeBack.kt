package app.widgets.welcomeBack

import app.UIColorsObject.Colors
import app.ui.styles.ButtonStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.text.FontWeight
import tornadofx.*
import app.widgets.profileIcon.ProfileIcon
import tornadofx.FX.Companion.messages

class WelcomeBack : HBox() {
    private val rad = 125.0
    private val bigIcons = borderpane() {

        alignment = Pos.CENTER

        style {
            backgroundColor += c(Colors["base"])
        }
        val iconHash = ProfileIcon("12345678901", 150.0, true)
        iconHash.alignment = Pos.CENTER

        top = iconHash
        center = label(messages["welcome"]) {
            style {
                fontSize = 32.0.px
                FontWeight.BOLD
            }
        }
        val homeIcon = MaterialIconView(MaterialIcon.HOME, "25px")
        bottom = hbox {
            alignment = Pos.CENTER
            button("", homeIcon) {
                importStylesheet(ButtonStyles::class)
                addClass(ButtonStyles.rectangleButtonAlternate)
                style {
                    minWidth = 175.0.px
                    homeIcon.fill = c(Colors["base"])
                }
            }
        }
        //prevents from spreading out to take up whole screen when window maximized
        //note: 100 extra pixels hard coded in for space,
        // but we may change this val depending on size of home button and text
        setMaxSize(2 * rad, 3 * rad);
        setPrefSize(2 * rad + 100, 3 * rad);
        usePrefSize
    }
}

