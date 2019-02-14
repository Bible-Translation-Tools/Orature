package org.wycliffeassociates.otter.jvm.app.ui.viewtakes.view

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.effect.Effect
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.Screen
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class ViewTakesStyles : Stylesheet() {
    companion object {
        val viewTakesTitle by cssclass()
        val deleteButton by cssclass()
        val dragTarget by cssclass()
        val takeCard by cssclass()
        val badge by cssclass()
        val placeholder by cssclass()
        val headerContainer by cssclass()
        val selectedTakeContainer by cssclass()
        val takeFlowPane by cssclass()
        val glow by cssclass()
        val recordButton by cssclass()
        val playPauseButton by cssclass()
        val navigationButton by cssclass()
        val newTakeCard by cssclass()
        val scrollpane by cssclass()
    }

    init {
        viewTakesTitle {
            fontSize = 40.px
            textFill = AppTheme.colors.defaultText
            child("*") {
                fill = AppTheme.colors.defaultText
            }
        }
        takeFlowPane {
            borderColor += box(Color.LIGHTGRAY)
            borderWidth += box(0.px, 0.px, 0.px, 0.px)
            backgroundColor += AppTheme.colors.defaultBackground
            spacing = 10.px
            padding = box(10.px, 150.px)
            vgap = 16.px
            hgap = 16.px
        }

        scrollpane {
            maxHeight = Screen.getPrimary().visualBounds.height.px / 2.5
            backgroundColor += Color.TRANSPARENT
        }

        glow {
            effect = DropShadow(5.0, AppTheme.colors.appBlue)
        }

        dragTarget {
            backgroundColor += AppTheme.colors.cardBackground.deriveColor(0.0, 1.0, 1.0, 0.8)
            borderRadius += box(10.px)
            backgroundRadius += box(10.px)
            minHeight = 200.px
            minWidth = 348.px
            maxHeight = minHeight
            maxWidth = minWidth
            label {
                fontSize = 16.px
            }
            child("*") {
                fill = AppTheme.colors.appBlue
            }
        }

        takeCard {
            borderRadius += box(10.px)
            borderColor += box(AppTheme.colors.imagePlaceholder)
            borderWidth += box(1.px)
            backgroundColor += AppTheme.colors.cardBackground
            label {
                textFill = AppTheme.colors.defaultText
            }
            badge {
                backgroundColor += AppTheme.colors.appRed
            }
            button {
                and(deleteButton) {
                    child("*") {
                        fill = AppTheme.colors.defaultText
                    }
                }
                and(playPauseButton) {
                    child("*") {
                        fill = AppTheme.colors.appRed
                    }
                }
            }
        }

        newTakeCard {
            minHeight = 200.px
            minWidth = 348.px
            borderRadius += box(5.0.px)
            backgroundRadius += box(5.0.px)
            effect = DropShadow(2.0,2.0,2.0,AppTheme.colors.dropShadow)
            backgroundColor += AppTheme.colors.white
            button {
                minHeight = 40.px
                minWidth = 158.px
                backgroundColor += AppTheme.colors.appRed
                fontSize = 16.px
                //overwrite dropshadow on newtakecard
                effect = DropShadow(0.0,0.0,0.0,AppTheme.colors.dropShadow)
                child("*") {
                    fill = AppTheme.colors.white
                }
            }

            label {
                fontSize = 25.px
                fontWeight = FontWeight.BOLD
                effect = DropShadow(0.0,0.0,0.0,AppTheme.colors.dropShadow)
            }
        }

        placeholder {
            backgroundColor += AppTheme.colors.imagePlaceholder
            borderRadius += box(10.px)
            backgroundRadius += box(10.px)
            minHeight = 200.px
            minWidth = 348.px
        }

        headerContainer {
            padding = box(20.px)
            spacing = 20.px
            alignment = Pos.CENTER_LEFT
        }

        selectedTakeContainer {
            backgroundColor += AppTheme.colors.defaultBackground
            padding = box(20.px)
            alignment = Pos.CENTER_LEFT
            label {
                textFill = AppTheme.colors.defaultText
            }
        }

        recordButton {
            backgroundRadius += box(25.px)
            borderRadius += box(25.px)
            backgroundColor += AppTheme.colors.base
            minHeight = 50.px
            minWidth = 50.px
            maxHeight = 50.px
            maxWidth = 50.px
            cursor = Cursor.HAND
            effect = DropShadow(10.0, AppTheme.colors.dropShadow)
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