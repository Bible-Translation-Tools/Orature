package org.wycliffeassociates.otter.jvm.app.ui.takemanagement.view

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class RecordScriptureStyles : Stylesheet() {
    companion object {
        val background by cssclass()
        val viewTakesTitle by cssclass()
        val dragTarget by cssclass()
        val headerContainer by cssclass()
        val takeGrid by cssclass()
        val glow by cssclass()
        val recordTakeButton by cssclass()
        val navigationButton by cssclass()
        val newTakeCard by cssclass()
        val scrollpane by cssclass()
        val pageTop by cssclass()

        val takeMaxWidth = 348.px
        val takeMinHeight = 200.px

        fun takeWidthHeight() = mixin {
            minHeight = takeMinHeight
            maxWidth = takeMaxWidth
            prefWidth = maxWidth
            maxHeight = minHeight
        }

        fun takeRadius() = mixin {
            borderRadius += box(10.px)
            backgroundRadius += box(10.px)
        }
    }

    init {
        background {
            backgroundColor += AppTheme.colors.white
        }
        viewTakesTitle {
            fontSize = 40.px
            textFill = AppTheme.colors.defaultText
            child("*") {
                fill = AppTheme.colors.defaultText
            }
        }
        takeGrid {
            borderColor += box(Color.LIGHTGRAY)
            borderWidth += box(0.px, 0.px, 0.px, 0.px)
            backgroundColor += AppTheme.colors.white
            spacing = 10.px
            alignment = Pos.CENTER_LEFT
            vgap = 16.px
            hgap = 16.px
            padding = box(5.0.px)
        }

        pageTop {
            minHeight = 400.px
            maxHeight = minHeight
        }

        scrollpane {
            backgroundColor += Color.TRANSPARENT
        }

        glow {
            effect = DropShadow(5.0, AppTheme.colors.appBlue)
        }

        dragTarget {
            +takeWidthHeight()
            +takeRadius()
            backgroundColor += AppTheme.colors.cardBackground.deriveColor(0.0, 1.0, 1.0, 0.8)
            label {
                fontSize = 16.px
            }
            child("*") {
                fill = AppTheme.colors.appBlue
            }
        }

        newTakeCard {
            +takeWidthHeight()
            borderRadius += box(5.0.px)
            borderColor += box(AppTheme.colors.defaultBackground)
            borderWidth += box(1.px)
            backgroundRadius += box(5.0.px)
            effect = DropShadow(2.0,2.0,2.0,AppTheme.colors.dropShadow)
            backgroundColor += AppTheme.colors.white
            button {
                minHeight = 40.px
                minWidth = 158.px
                backgroundColor += AppTheme.colors.appRed
                fontSize = 16.px
                child("*") {
                    fill = AppTheme.colors.white
                }
            }

            label {
                fontSize = 25.px
                fontWeight = FontWeight.BOLD
            }
        }

        headerContainer {
            padding = box(20.px)
            spacing = 20.px
            alignment = Pos.CENTER_LEFT
        }

        recordTakeButton {
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            backgroundColor += AppTheme.colors.base
            minHeight = 50.px
            minWidth = 50.px
            maxHeight = 50.px
            maxWidth = 50.px
            cursor = Cursor.HAND
            unsafe("-jfx-button-type", raw("RAISED"))
            child("*") {
                fill = AppTheme.colors.appRed
            }
        }

        navigationButton {
            minHeight = 40.px
            minWidth = 187.0.px
            backgroundColor += AppTheme.colors.white
            borderColor += box(AppTheme.colors.appRed)
            borderWidth += box(0.5.px)
            borderRadius += box(5.0.px)
            backgroundRadius += box(5.0.px)
            child("*") {
                fill = AppTheme.colors.appRed
            }
            fontSize = 14.px
        }
    }
}