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
package org.wycliffeassociates.otter.jvm.controls.dragtarget

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import tornadofx.*
import tornadofx.Stylesheet.Companion.label

object DragTargetStyles {
    private fun borderGlowMixin(glowColor: Color) = mixin { effect = DropShadow(5.0, glowColor) }

    private fun dragTargetOverlayMixin(glowColor: Color): CssSelectionBlock {
        return mixin {
            backgroundColor += Color.WHITE.deriveColor(
                0.0,
                1.0,
                1.0,
                0.8
            )
            label {
                fontSize = 16.px
            }
            child("*") {
                fill = glowColor
            }
        }
    }

    class Resource : Stylesheet() {

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

        companion object {
            val resourceDragTargetSize by cssclass()
            val selectedResourceTakePlaceHolder by cssclass()
            val resourceDragTargetOverlay by cssclass()
            val borderGlow by cssclass()
        }

        private val glowColor = c("#0094F0")

        init {
            resourceDragTargetSize {
                +takeWidthHeight()
            }
            selectedResourceTakePlaceHolder {
                +takeRadius()
                alignment = Pos.CENTER
                backgroundColor += c("#EEEEEE")
                borderColor += box(Color.DARKGRAY)
                borderStyle += BorderStrokeStyle.DASHED
                borderWidth += box(2.px)
                fontStyle = FontPosture.ITALIC
            }
            borderGlow {
                +borderGlowMixin(glowColor)
            }
            resourceDragTargetOverlay {
                +dragTargetOverlayMixin(glowColor)
                +takeRadius()
            }
        }
    }

    class Scripture : Stylesheet() {

        val takeMaxWidth = 332.px
        val takeMinHeight = 148.px

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

        companion object {
            val dragTargetSize by cssclass()
            val selectedTakePlaceHolder by cssclass()
            val dragTargetOverlay by cssclass()
            val borderGlow by cssclass()
        }

        private val glowColor = c("#0094F0")

        init {
            dragTargetSize {
                +takeWidthHeight()
            }
            selectedTakePlaceHolder {
                +takeRadius()
                backgroundColor += c("#DDDDDD")
            }
            borderGlow {
                +borderGlowMixin(glowColor)
            }
            dragTargetOverlay {
                +dragTargetOverlayMixin(glowColor)
                +takeRadius()
            }
        }
    }
}