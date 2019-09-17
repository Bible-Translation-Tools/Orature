package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import javafx.geometry.Pos
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

class RecordResourceStyles : Stylesheet() {
    companion object {
        val takesTab by cssclass()
        val leftRegionContainer by cssclass()
        val rightRegion by cssclass()
        val contentText by cssclass()
        val newTakeRegion by cssclass()
        val contentScrollPane by cssclass()
        val takesList by cssclass()

        private val takeMaxWidth = 500.px
        private val takeMinHeight = 80.px

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
