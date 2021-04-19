package org.wycliffeassociates.otter.jvm.controls.stepper

import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import tornadofx.*

class Stepper() : HBox() {
    var steps = observableListOf<Step>()

    val fillColorProperty = SimpleObjectProperty<Color>()
    var fillColor by fillColorProperty

    init {
        alignment = Pos.CENTER
        spacing = 16.0
        steps.onChange {
            it.list.forEach {
                add(it)
            }
        }
    }

    fun step(separator: Boolean = true, init: Step.() -> Unit = {}): Step {
        val st = Step(separator)
        st.init()
        steps.add(st)
        return st
    }
}

fun stepper(init: Stepper.() -> Unit = {}): Stepper {
    val ps = Stepper()
    ps.init()
    return ps
}
