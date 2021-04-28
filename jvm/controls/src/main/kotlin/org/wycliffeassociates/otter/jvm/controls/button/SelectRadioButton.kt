package org.wycliffeassociates.otter.jvm.controls.button

import javafx.scene.control.Skin
import javafx.scene.control.ToggleButton
import org.wycliffeassociates.otter.jvm.controls.skins.button.SelectRadioButtonSkin
import tornadofx.*

class SelectRadioButton : ToggleButton() {

    init {
        importStylesheet(javaClass.getResource("/css/select-radio-button.css").toExternalForm())
        styleClass.setAll("select-radio-button")
    }

    override fun createDefaultSkin(): Skin<*> {
        return SelectRadioButtonSkin(this)
    }
}
