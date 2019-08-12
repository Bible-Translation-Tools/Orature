package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class RecordResourceStyles : Stylesheet() {
    companion object {
        val takesTab by cssclass()
        val leftRegionContainer by cssclass()
        val rightRegion by cssclass()
        val selectedTakePlaceholder by cssclass()
        val contentText by cssclass()
        val newTakeRegion by cssclass()
        val contentScrollPane by cssclass()
        val takesList by cssclass()
        val glow by cssclass()
        val dragTarget by cssclass()

        val takeMaxWidth = 500.px
        val takeMinHeight = 80.px

        fun takeWidthHeight() = mixin {
            minHeight = takeMinHeight
            maxWidth = takeMaxWidth
            minWidth = maxWidth
            maxHeight = minHeight
        }

        fun takeRadius() = mixin {
            borderRadius += box(5.px)
            backgroundRadius += box(5.px)
        }
    }

    private val topMargin = 30.px
    private val bottomMargin = 15.px
    private val leftRegionLeftMargin = 80.px
    private val leftRegionRightMargin = 100.px

    init {
        takesTab {
            backgroundColor += AppTheme.colors.white
        }

        leftRegionContainer {
            padding = box(topMargin, 0.px, 0.px, 0.px)
            borderColor += box(
                Color.TRANSPARENT,
                AppTheme.colors.lightBackground,
                AppTheme.colors.lightBackground,
                Color.TRANSPARENT
            )
        }

        rightRegion {
            padding = box(topMargin, 70.px, 30.px, 30.px)
        }

        selectedTakePlaceholder {
            +takeWidthHeight()
            +takeRadius()
            alignment = Pos.CENTER
            backgroundColor += AppTheme.colors.defaultBackground
            borderColor += box(Color.DARKGRAY)
            borderStyle += BorderStrokeStyle.DASHED
            borderWidth += box(2.px)
            fontStyle = FontPosture.ITALIC
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
                // TODO: This won't always be blue
                fill = AppTheme.colors.appBlue
            }
        }

        contentText {
            fontSize = 24.px // If you put this in contentScrollPane, the scroll bar gets very big
        }

        newTakeRegion {
            alignment = Pos.CENTER
            borderColor += box(AppTheme.colors.lightBackground, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT)
            borderWidth += box(3.px, 0.px, 0.px, 0.px)
            padding = box(topMargin, leftRegionLeftMargin, bottomMargin, leftRegionRightMargin)
        }

        contentScrollPane {
            padding = box(15.px, leftRegionLeftMargin, 30.px, leftRegionRightMargin)
            backgroundColor += Color.TRANSPARENT
            viewport {
                backgroundColor += Color.TRANSPARENT
            }
        }

        takesList {
            backgroundColor += Color.TRANSPARENT
        }

        listCell {
            backgroundColor += Color.TRANSPARENT
            padding = box(0.px, 0.px, 15.px, 0.px)
        }
    }
}
