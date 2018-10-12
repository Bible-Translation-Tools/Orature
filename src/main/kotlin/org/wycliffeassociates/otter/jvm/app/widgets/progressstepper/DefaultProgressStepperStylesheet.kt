package org.wycliffeassociates.otter.jvm.app.widgets.progressstepper

import javafx.geometry.Pos
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import tornadofx.*

class DefaultProgressStepperStylesheet : Stylesheet() {
    companion object {
        val progressStepper by cssclass()
        val completed by csspseudoclass("completed")
        val defaultHighlightColor = c("2962ff")
    }

    init {
        progressStepper {
            spacing = 10.px
            prefHeight = 80.px
            alignment = Pos.CENTER
            line {
                strokeWidth = 2.px
                stroke = Color.LIGHTGRAY
                and(completed) {
                    stroke = defaultHighlightColor
                }
            }
            button {
                maxHeight = 32.px
                maxWidth = 32.px
                minHeight = 32.px
                minWidth = 32.px
                backgroundRadius += box(16.px)
                backgroundColor += Color.TRANSPARENT
                borderRadius += box(16.px)
                borderWidth += box(1.px)
                borderColor += box(defaultHighlightColor)
                borderStyle += BorderStrokeStyle.SOLID
                child("*") {
                    fill = defaultHighlightColor
                }
                and(completed, hover) {
                    backgroundColor += defaultHighlightColor
                    child("*") {
                        fill = Color.WHITE
                    }
                }
            }
        }
    }
}