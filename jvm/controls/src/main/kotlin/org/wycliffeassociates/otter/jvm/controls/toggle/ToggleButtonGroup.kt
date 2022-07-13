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
package org.wycliffeassociates.otter.jvm.controls.toggle

import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.addClass
import tornadofx.bindChildren
import tornadofx.hgrow
import tornadofx.observableListOf
import tornadofx.opcr
import tornadofx.toggleClass
import tornadofx.vgrow
import tornadofx.whenSelected

class ToggleButtonGroup(
    val items: ObservableList<ToggleButtonData> = observableListOf()
) : HBox() {
    val tg = ToggleGroup()

    init {
        addClass("wa-toggle-btn-group")
        hgrow = Priority.ALWAYS

        bindChildren(items) { data ->
            RadioToggleButton(data.title).apply {
                toggleGroup = tg

                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

                whenSelected {
                    data.onAction()
                }

                isSelected = data.isDefaultSelected

                // styling for first & last element
                toggleClass("first-child", items.indexOf(data) == 0)
                toggleClass("last-child", items.indexOf(data) == items.size - 1)
            }
        }
    }
}

private class RadioToggleButton(text: String = "") : ToggleButton(text) {

    init {
        addClass("btn", "btn--borderless", "custom-toggle-btn")
    }

    override fun fire() {
        // prevent "unselect" a selected toggle
        if (toggleGroup == null || !isSelected) {
            super.fire()
        }
    }
}

fun EventTarget.togglebuttongroup(values: ObservableList<ToggleButtonData>? = null, spacing: Double? = null, op: HBox.() -> Unit): HBox {
    val toggleBtnGroup = if (values != null) {
        ToggleButtonGroup(values)
    } else {
        ToggleButtonGroup()
    }
    if (spacing != null) toggleBtnGroup.spacing = spacing
    return opcr(this, toggleBtnGroup, op)
}