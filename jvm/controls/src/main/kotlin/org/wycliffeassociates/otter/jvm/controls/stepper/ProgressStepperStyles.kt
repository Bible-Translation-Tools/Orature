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
package org.wycliffeassociates.otter.jvm.controls.stepper

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
