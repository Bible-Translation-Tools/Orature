package org.wycliffeassociates.otter.jvm.controls.button

import javafx.scene.control.CheckBox
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.button.CheckboxButtonSkin

class CheckboxButton : CheckBox() {

    init {
        styleClass.setAll("checkbox-button")
    }

    override fun createDefaultSkin(): Skin<*> {
        return CheckboxButtonSkin(this)
    }
}
