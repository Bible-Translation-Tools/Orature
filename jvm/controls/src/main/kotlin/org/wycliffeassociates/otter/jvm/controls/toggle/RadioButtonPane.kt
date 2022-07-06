package org.wycliffeassociates.otter.jvm.controls.toggle

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.addClass
import tornadofx.bindChildren
import tornadofx.booleanBinding
import tornadofx.hgrow
import tornadofx.objectBinding
import tornadofx.observableListOf
import tornadofx.radiobutton
import tornadofx.togglebutton
import tornadofx.vgrow
import tornadofx.whenSelected

class RadioButtonPane : HBox() {
    val list = observableListOf<ToggleButtonData>()
    val tg = ToggleGroup()

    init {
        addClass("wa-toggle-button-pane")
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