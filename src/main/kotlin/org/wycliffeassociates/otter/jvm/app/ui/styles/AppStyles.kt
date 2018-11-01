package org.wycliffeassociates.otter.jvm.app.ui.styles

import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*

class AppStyles : Stylesheet() {

    companion object {
        val noProjectsLabel by cssclass()
        val tryCreatingLabel by cssclass()
        val addProjectButton by cssclass()
        val refreshButton by cssclass()
        val wizardCardGraphicsContainer by cssclass()
        val wizardCard by cssclass()
        val noResource by cssclass()
        val projectCard by cssclass()
        val recordButton by cssclass()
        val projectCardTitle by cssclass()
        val projectCardLanguage by cssclass()
        val projectGraphicContainer by cssclass()
        val progressOverlay by cssclass()
    }

    init {
        noProjectsLabel {
            fontSize = 30.px
            fontWeight = FontWeight.BOLD
        }

        tryCreatingLabel {
            fontSize = 20.px
        }

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
                textFill = c(UIColorsObject.Colors["baseText"])
                and(projectCardTitle) {
                    fontWeight = FontWeight.BOLD
                    fontSize = 16.px
                }
                and(projectCardLanguage) {
                    fontWeight = FontWeight.NORMAL
                    textFill = Color.GRAY
                }
            }

            s(".jfx-button") {
                minHeight = 40.px
                maxWidth = Double.MAX_VALUE.px
                backgroundColor += c(UIColorsObject.Colors["primary"])
                textFill = c(UIColorsObject.Colors["base"])
                cursor = Cursor.HAND
                fontSize = 16.px
                fontWeight = FontWeight.BOLD
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
                fontWeight = FontWeight.BOLD
                fontSize = 16.px
            }
            s(".jfx-button") {
                minHeight = 40.0.px
                maxWidth = Double.MAX_VALUE.px
                backgroundColor += c(UIColorsObject.Colors["primary"])
                textFill = c(UIColorsObject.Colors["base"])
                cursor = Cursor.HAND
                fontSize = 16.px
                fontWeight = FontWeight.BOLD
            }
        }

        noResource {
            padding = box(50.px)
            backgroundColor += c(Colors["base"])
            fontSize = 24.px
            fontWeight = FontWeight.BOLD
            textFill = c(Colors["primary"])
        }
        progressOverlay {
            fillHeight = true
            fillWidth = true
            s("*") {
                fill = Color.WHITE
            }
        }
        // Material design scroll bar
        scrollBar {
            backgroundColor += Color.TRANSPARENT
            padding = box(0.px, 4.px)
            prefWidth = 16.px
            thumb {
                backgroundColor += Color.DARKGRAY
                backgroundRadius += box(10.px)
            }
            incrementArrow {
               visibility = FXVisibility.COLLAPSE
            }
            decrementArrow {
                visibility = FXVisibility.COLLAPSE
            }
        }

        scrollPane {
            backgroundColor += Color.TRANSPARENT
        }

        // Load the fonts
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Regular.ttf"), 10.0)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Bold.ttf"), 10.0)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-BoldItalic.ttf"), 10.0)
        Font.loadFont(ClassLoader.getSystemResourceAsStream("fonts/NotoSans-Italic.ttf"), 10.0)
        root {
            font = Font.font("Noto Sans", 10.0)
        }
    }
}
