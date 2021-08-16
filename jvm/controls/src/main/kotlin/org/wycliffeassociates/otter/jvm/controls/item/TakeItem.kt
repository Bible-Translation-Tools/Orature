package org.wycliffeassociates.otter.jvm.controls.item

import com.jfoenix.controls.JFXSlider
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class TakeItem(private val group: ToggleGroup) : HBox() {
    val audioProperty = SimpleStringProperty()

    init {
        styleClass.setAll("take-item")

        add(
            button {
                addClass("btn", "btn--icon")
                graphic = FontIcon(MaterialDesign.MDI_PLAY)
            }
        )
        add(
            JFXSlider().apply {
                addClass("wa-slider")
                hgrow = Priority.ALWAYS
            }
        )
        add(
            radiobutton {
                addClass("wa-radio", "wa-radio--bordered")
                toggleGroup = group
            }
        )
    }
}
