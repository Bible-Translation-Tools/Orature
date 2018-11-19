package org.wycliffeassociates.otter.jvm.app.ui.projecthome.view

import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import tornadofx.*

class ProjectHomeStyles : Stylesheet() {
    companion object {
        val noProjectsLabel by cssclass()
        val tryCreatingLabel by cssclass()
        val addProjectButton by cssclass()
        val projectCard by cssclass()
        val projectCardTitle by cssclass()
        val projectCardLanguage by cssclass()
        val projectGraphicContainer by cssclass()
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
    }
}