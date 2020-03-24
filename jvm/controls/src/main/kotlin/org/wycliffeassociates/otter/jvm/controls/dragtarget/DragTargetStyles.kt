package org.wycliffeassociates.otter.jvm.controls.dragtarget

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import tornadofx.*
import tornadofx.Stylesheet.Companion.label

object DragTargetStyles {
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
                backgroundColor += c("#EEEEEE")
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