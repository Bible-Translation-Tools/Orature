package org.wycliffeassociates.otter.jvm.app.ui.styles

import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*

class AppStyles : Stylesheet() {

    companion object {
        val addProjectButton by cssclass()
        val refreshButton by cssclass()
        val cardButton by cssclass()
        val wizardCardGraphicsContainer by cssclass()
        val wizardCard by cssclass()
        val noResource by cssclass()
        val projectCard by cssclass()
        val recordButton by cssclass()
        val projectGraphicContainer by cssclass()
    }

    init {
        addProjectButton {
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            backgroundColor += c(UIColorsObject.Colors["primary"])
            minHeight = 50.px
            minWidth = 50.px
            maxHeight = 50.px
            maxWidth = 50.px
            cursor = Cursor.HAND
            unsafe("-jfx-button-type", raw("RAISED"))
            child("*") {
                fill = c(UIColorsObject.Colors["base"])
            }
        }

        recordButton {
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            backgroundColor += c(UIColorsObject.Colors["base"])
            minHeight = 50.px
            minWidth = 50.px
            maxHeight = 50.px
            maxWidth = 50.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, Color.GRAY)
            unsafe("-jfx-button-type", raw("RAISED"))
            child("*") {
                fill = c(UIColorsObject.Colors["primary"])
            }
        }

        refreshButton {
            prefHeight = 40.0.px
            //backgroundColor += c(UIColorsObject.Colors["primary"])
            unsafe("-jfx-button-type", raw("FLAT"))
            child("*") {
                fill = c(UIColorsObject.Colors["primary"])
            }
        }

        projectCard {
            prefWidth = 232.px
            prefHeight = 300.px
            backgroundColor += c(UIColorsObject.Colors["base"])
            padding = box(10.px)
            backgroundRadius += box(10.px)
            spacing = 10.px
            projectGraphicContainer {
                backgroundRadius += box(10.px)
                backgroundColor += c(UIColorsObject.Colors["baseLight"])
            }
            label {
                textFill = Color.BLACK
            }
            s(".jfx-button") {
                minHeight = 40.0.px
                maxWidth = Double.MAX_VALUE.px
                backgroundColor += c(UIColorsObject.Colors["primary"])
                textFill = c(UIColorsObject.Colors["base"])
                cursor = Cursor.HAND
                fontSize = (16.0.px)
                fontWeight = FontWeight.BLACK
            }
        }

        wizardCard {
            prefWidth = 280.px
            prefHeight = 300.px
            backgroundColor += c(UIColorsObject.Colors["base"])
            padding = box(10.px)
            backgroundRadius += box(10.px)
            spacing = 10.px
            wizardCardGraphicsContainer {
                backgroundRadius += box(10.px)
                backgroundColor += c(UIColorsObject.Colors["base"])
            }
            label {
                textFill = Color.BLACK
            }
            s(".jfx-button") {
                minHeight = 40.0.px
                maxWidth = Double.MAX_VALUE.px
                backgroundColor += c(UIColorsObject.Colors["primary"])
                textFill = c(UIColorsObject.Colors["base"])
                cursor = Cursor.HAND
                fontSize = (16.0.px)
                fontWeight = FontWeight.BLACK
            }
        }

        noResource {
            padding = box(50.px)
            backgroundColor += c(Colors["base"])
            fontSize = 24.px
            fontWeight = FontWeight.BOLD
            textFill = c(Colors["primary"])
        }

    }
}
