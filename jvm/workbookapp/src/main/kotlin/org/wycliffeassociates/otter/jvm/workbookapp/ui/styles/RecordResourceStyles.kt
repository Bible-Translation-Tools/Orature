/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.styles

import javafx.geometry.Pos
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
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
        val bottomButton by cssclass()

        private val takeMaxWidth = 500.px
        private val takeMinHeight = 80.px

        fun takeWidthHeight() = mixin {
            minHeight = takeMinHeight
            maxWidth = takeMaxWidth
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
            backgroundColor += Color.valueOf("#e7ecf5")
        }

        rightRegion {
            padding = box(topMargin, 70.px, 30.px, 30.px)
            borderColor += box(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                AppTheme.colors.lightBackground,
                AppTheme.colors.lightBackground
            )
        }

        contentText {
            fontSize = 24.px // If you put this in contentScrollPane, the scroll bar gets very big
        }

        newTakeRegion {
            alignment = Pos.CENTER
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

        bottomButton {
            borderColor += box(AppTheme.colors.appBlue)
            borderWidth += box(2.px)
            borderRadius += box(5.px)
            child("*") {
                fill = AppTheme.colors.appBlue
                fontSize = 16.px
            }
            maxWidth = 500.px
        }
    }
}
