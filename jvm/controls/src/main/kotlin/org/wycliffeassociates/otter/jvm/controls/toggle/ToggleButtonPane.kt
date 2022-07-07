package org.wycliffeassociates.otter.jvm.controls.toggle

import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.addClass
import tornadofx.bindChildren
import tornadofx.hgrow
import tornadofx.observableListOf
import tornadofx.toggleClass
import tornadofx.vgrow
import tornadofx.whenSelected

class ToggleButtonPane : HBox() {
    val list = observableListOf<ToggleButtonData>()
    val tg = ToggleGroup()

    init {
        addClass("wa-toggle-btn-pane")
        hgrow = Priority.ALWAYS

        bindChildren(list) { data ->
            RadioToggleButton(data.title).apply {
                toggleGroup = tg

                hgrow = Priority.ALWAYS
                vgrow = Priority.ALWAYS

                whenSelected {
                    data.onAction()
                }

                isSelected = data.isDefaultSelected

                toggleClass("toggle-btn--first-child", list.indexOf(data) == 0)
                toggleClass("toggle-btn--last-child", list.indexOf(data) == list.size - 1)
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