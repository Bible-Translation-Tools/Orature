/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

class Step(separator: Boolean) : HBox() {

    val stepTextProperty = SimpleStringProperty()
    var stepText by stepTextProperty

    val stepGraphicProperty = SimpleObjectProperty<Node>()
    var stepGraphic by stepGraphicProperty

    val completedTextProperty = SimpleStringProperty()
    var completedText by completedTextProperty

    init {
        importStylesheet<ProgressStepperStyles>()
        alignment = Pos.CENTER
        spacing = 10.0
        stackpane {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS

            label("") {
                graphicProperty().bind(stepGraphicProperty)
                visibleWhen { completedTextProperty.isNull }
                addClass(ProgressStepperStyles.graphicLabel)
            }
            label("") {
                graphic = ProgressStepperStyles.checkIcon()
                visibleWhen { completedTextProperty.isNotNull }
                addClass(ProgressStepperStyles.graphicLabel)
            }
            addClass(ProgressStepperStyles.stepGraphicContainer)
        }

        stackpane {
            label(stepTextProperty) {
                addClass(ProgressStepperStyles.stepTextLabel)
                visibleWhen { completedTextProperty.isNull }
            }
            label(completedTextProperty) {
                addClass(ProgressStepperStyles.completedTextLabel)
                visibleWhen { completedTextProperty.isNotNull }
            }
        }
        if (separator) {
            rectangle {
                width = 100.0
                height = 2.5
                arcWidth = 5.0
                arcHeight = 5.0
                fill = Color.WHITE
                toggleClass(ProgressStepperStyles.completedBar,
                    completedTextProperty.booleanBinding { it != null })
            }
        }
    }
}
