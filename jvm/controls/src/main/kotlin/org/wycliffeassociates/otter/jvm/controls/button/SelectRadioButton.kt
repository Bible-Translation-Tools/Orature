package org.wycliffeassociates.otter.jvm.controls.button

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Skin
import javafx.scene.control.ToggleButton
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.skins.button.SelectRadioButtonSkin

class SelectRadioButton : ToggleButton() {

    val btnTextProperty = SimpleStringProperty()
    val btnIconProperty = SimpleObjectProperty<FontIcon>()

    init {
        styleClass.setAll("select-radio-button")
    }

    override fun createDefaultSkin(): Skin<*> {
        return SelectRadioButtonSkin(this)
    }
}
