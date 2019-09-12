package org.wycliffeassociates.otter.jvm.app.widgets.progressstepper

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.paint.Color
import tornadofx.*

class ProgressStepperStyles : Stylesheet() {

    companion object {
        fun checkIcon() = MaterialIconView(MaterialIcon.CHECK, "16px")

        val graphicLabel by cssclass()
        val stepGraphicContainer by cssclass()
        val completedBar by cssclass()
        val incompleteBar by cssclass()
        val completedTextLabel by cssclass()
        val stepTextLabel by cssclass()
    }

    init {
        graphicLabel {
            textFill = Color.WHITE
            child("*") {
                fill = Color.WHITE
            }
        }

        stepGraphicContainer {
            backgroundColor += c("#CC4141")
            backgroundRadius += box(100.percent)
            alignment = Pos.CENTER
            minHeight = 24.0.px
            minWidth = 24.0.px
        }

        completedBar {
            fill = c("#CC4141")
        }

        incompleteBar {
            fill = Color.WHITE
        }

        completedTextLabel {
            textFill = c("#CC4141")
        }

        stepTextLabel {
            textFill = Color.BLACK
        }
    }
}