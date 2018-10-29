package org.wycliffeassociates.otter.jvm.app.ui.menu

import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.Stylesheet
import tornadofx.*

class MainMenuStylesheet : Stylesheet() {
    init {
        menuBar {
            backgroundColor += Color.WHITE
            menu {
                fontSize = 16.px
                and(hover, showing) {
                    backgroundColor += c(Colors["primary"])
                }
                maxHeight = Double.MAX_VALUE.px
            }
            menuItem {
                fontSize = 14.px
                and(hover, focused, showing) {
                    backgroundColor += c(Colors["primary"])
                }
            }
        }
    }
}