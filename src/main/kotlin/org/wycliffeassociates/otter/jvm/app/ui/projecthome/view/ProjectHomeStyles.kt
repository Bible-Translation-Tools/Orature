package org.wycliffeassociates.otter.jvm.app.ui.projecthome.view

import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
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
            textFill = AppTheme.colors.defaultText
        }

        tryCreatingLabel {
            fontSize = 20.px
            textFill = AppTheme.colors.defaultText
        }

        addProjectButton {
            unsafe("-jfx-button-type", raw("RAISED"))
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            backgroundColor += AppTheme.colors.appRed
            minHeight = 50.px
            minWidth = 50.px
            maxHeight = 50.px
            maxWidth = 50.px
            cursor = Cursor.HAND
            child("*") {
                fill = AppTheme.colors.white
            }
        }

        projectCard {
            prefWidth = 232.px
            prefHeight = 300.px
            backgroundColor += AppTheme.colors.cardBackground
            padding = box(10.px)
            backgroundRadius += box(10.px)
            spacing = 10.px
            projectGraphicContainer {
                backgroundRadius += box(10.px)
                backgroundColor += AppTheme.colors.imagePlaceholder
                child("*") {
                    fill = AppTheme.colors.defaultText
                }
            }
            label {
                textFill = AppTheme.colors.defaultText
                and(projectCardTitle) {
                    fontWeight = FontWeight.BOLD
                    fontSize = 16.px
                }
                and(projectCardLanguage) {
                    fontWeight = FontWeight.NORMAL
                    textFill = AppTheme.colors.subtitle
                }
            }

            s(".jfx-button") {
                minHeight = 40.px
                maxWidth = Double.MAX_VALUE.px
                backgroundColor += AppTheme.colors.appRed
                textFill = AppTheme.colors.white
                cursor = Cursor.HAND
                fontSize = 16.px
                fontWeight = FontWeight.BOLD
            }
        }
    }
}