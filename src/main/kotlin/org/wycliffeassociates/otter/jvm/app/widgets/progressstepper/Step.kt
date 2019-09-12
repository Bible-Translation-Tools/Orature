package org.wycliffeassociates.otter.jvm.app.widgets.progressstepper

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
