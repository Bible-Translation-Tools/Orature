package org.wycliffeassociates.otter.jvm.controls.dragtarget

import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view.RecordResourceStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordScriptureStyles
import tornadofx.*
import tornadofx.Stylesheet.Companion.label

object DragTargetStyles {
    private fun borderGlowMixin(glowColor: Color) = mixin { effect = DropShadow(5.0, glowColor) }

    private fun dragTargetOverlayMixin(glowColor: Color): CssSelectionBlock {
        return mixin {
            backgroundColor += AppTheme.colors.cardBackground.deriveColor(
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

        private val glowColor = AppTheme.colors.appBlue

        init {
            resourceDragTargetSize {
                +RecordResourceStyles.takeWidthHeight()
            }
            selectedResourceTakePlaceHolder {
                +RecordResourceStyles.takeRadius()
                alignment = Pos.CENTER
                backgroundColor += AppTheme.colors.defaultBackground
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
                +RecordResourceStyles.takeRadius()
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

        private val glowColor = AppTheme.colors.appBlue

        init {
            dragTargetSize {
                +RecordScriptureStyles.takeWidthHeight()
            }
            selectedTakePlaceHolder {
                +RecordScriptureStyles.takeRadius()
                backgroundColor += AppTheme.colors.defaultBackground
            }
            borderGlow {
                +borderGlowMixin(glowColor)
            }
            dragTargetOverlay {
                +dragTargetOverlayMixin(glowColor)
                +RecordScriptureStyles.takeRadius()
            }
        }
    }
}