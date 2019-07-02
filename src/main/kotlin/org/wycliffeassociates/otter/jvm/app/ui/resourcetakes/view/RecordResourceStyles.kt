package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import javafx.geometry.Pos
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import org.wycliffeassociates.otter.jvm.app.theme.AppTheme
import tornadofx.*

typealias LinearU = Dimension<Dimension.LinearUnits>

class RecordResourceStyles : Stylesheet() {
    companion object {
        val takesTab by cssclass()
        val leftRegionContainer by cssclass()
        val rightRegion by cssclass()
        val dragTarget by cssclass()
        val contentText by cssclass()
        val newTakeRegion by cssclass()
        val contentScrollPane by cssclass()
        val takesList by cssclass()

        val takeMaxWidth = 500.px
        val takeMinHeight = 80.px
    }

    private fun margin(top: LinearU, right: LinearU, bottom: LinearU, left: LinearU) = mixin {
        backgroundInsets += box(top, right, bottom, left)
        borderInsets += box(top, right, bottom, left)
    }

    private val topMargin = 15.px
    private val bottomMargin = 15.px
    private val leftRegionLeftMargin = 80.px
    private val leftRegionRightMargin = 100.px

    init {
        takesTab {
            backgroundColor += AppTheme.colors.white
        }

        leftRegionContainer {
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

        dragTarget {
            +margin(topMargin, leftRegionLeftMargin, 0.px, leftRegionRightMargin)
            alignment = Pos.CENTER
            backgroundColor += AppTheme.colors.defaultBackground
            maxWidth = takeMaxWidth + leftRegionLeftMargin + leftRegionRightMargin
            minHeight = takeMinHeight + topMargin
            borderStyle += BorderStrokeStyle.DASHED
            borderWidth += box(2.px)
            fontStyle = FontPosture.ITALIC
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
        }
    }
}
