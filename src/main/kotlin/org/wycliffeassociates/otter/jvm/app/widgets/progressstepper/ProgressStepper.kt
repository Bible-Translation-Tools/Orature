package org.wycliffeassociates.otter.jvm.app.widgets.progressstepper

import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.widgets.WidgetsStyles
import tornadofx.*
import tornadofx.Stylesheet.Companion.line
import tornadofx.Stylesheet.Companion.root


class ProgressStepper(private val steps: List<Node>) : HBox() {

    var activeIndex: Int by property(0)
    val activeIndexProperty = getProperty(ProgressStepper::activeIndex)

    init {
        importStylesheet<DefaultProgressStepperStylesheet>()
        addClass(DefaultProgressStepperStylesheet.progressStepper)
        steps.forEach { step ->
            button("", step) {
                action {
                    if (steps.indexOf(step) < activeIndex) activeIndex = steps.indexOf(step)
                }
                if (steps.indexOf(step) == activeIndex) addPseudoClass("completed")
                activeIndexProperty.onChange {
                    if (it ?: 0 >= steps.indexOf(step)) {
                        addPseudoClass("completed")
                    } else {
                        removePseudoClass("completed")
                    }
                }
            }
            if (step != steps.last()) {
                // Add a completion bar
                line {
                    startX = 0.0
                    startY = 0.0
                    endX = 100.0
                    endY = 0.0
                    addClass(line)
                    activeIndexProperty.onChange {
                        if (it ?: 0 > steps.indexOf(step)) {
                            addPseudoClass("completed")
                        } else {
                            removePseudoClass("completed")
                        }
                    }
                }
            }
        }
    }
}

fun Pane.progressstepper(steps: List<Node>, init: ProgressStepper.() -> Unit): ProgressStepper {
    val ps = ProgressStepper(steps)
    ps.init()
    add(ps)
    return ps
}