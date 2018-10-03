package org.wycliffeassociates.otter.jvm.app.widgets.progressstepper

import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.*
import org.wycliffeassociates.otter.jvm.app.widgets.WidgetsStyles
import tornadofx.*
import tornadofx.Stylesheet.Companion.root


class ProgressStepper(steps: List<Node>) : HBox() {

    var activeIndex = SimpleIntegerProperty(0)
    var activeIndexProperty by activeIndex


    var progressValue :Double by property(0.0)
    var progressValueProperty = getProperty(ProgressStepper::progressValue)

    var steps: List<Node> = steps
    var space: Double = 0.0


    init {
        importStylesheet<WidgetsStyles>()
        spaceNodes()
        with(root) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER
             anchorpane {

                stackpane {
                    setPrefSize(500.0, 80.0)
                    progressbar(0.0) {
                        addClass(WidgetsStyles.progressStepperBar)
                         progressValueProperty.onChange {
                            if(it != null) {
                                progress =it
                            }
                        }
                        setPrefSize(steps.size * space, 20.0)
                        setWidth(500.0)
                        hgrow = Priority.ALWAYS
                    }
                    hbox(space) {
                        anchorpaneConstraints {
                            topAnchor = 40.0
                            leftAnchor = 0.0
                        }
                        alignment = Pos.CENTER
                        steps.forEach {
                            var icon = it
                            button("", icon) {
                                style {
                                    backgroundRadius += box(20.0.px)
                                    prefHeight = 32.0.px
                                    prefWidth = 32.0.px
                                }
                                action {
                                    if (indexInParent < activeIndexProperty) {
                                        nextView(indexInParent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun setProgress(value: Double) {
        progressValue = value
    }

    fun nextView(index: Int) {
        activeIndex.set(index)
        setProgress((index.toDouble() / (steps.size - 1)))
    }

  private fun spaceNodes() {
        val width = 500.0
        val numNodes = steps.size
        space = width / (numNodes - 1)
    }
}

fun Pane.progressstepper(steps: List<Node>, init: ProgressStepper.() -> Unit): ProgressStepper {
    val ps = ProgressStepper(steps)
    ps.init()
    add(ps)
    return ps
}