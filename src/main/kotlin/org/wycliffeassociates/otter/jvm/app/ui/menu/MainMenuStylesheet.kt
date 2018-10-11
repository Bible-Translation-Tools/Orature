package org.wycliffeassociates.otter.jvm.app.ui.menu

import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.Stylesheet
import tornadofx.*

class MainMenuStylesheet : Stylesheet() {
    init {
        menuBar {
            backgroundColor += Color.WHITE
            prefHeight = 40.px
            menu {
                fontSize = 16.px
                and(hover, showing) {
                    backgroundColor += c(Colors["primary"])
                }
                padding = box(10.px, 20.px)
                maxHeight = Double.MAX_VALUE.px
            }
            menuItem {
                fontSize = 14.px
                padding = box(10.px, 20.px)
                and(hover, focused, showing) {
                    backgroundColor += c(Colors["primary"])
                }
            }
        }
    }
}