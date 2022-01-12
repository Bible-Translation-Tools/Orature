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
