package org.wycliffeassociates.otter.jvm.controls.button

import javafx.scene.control.Skin
import javafx.scene.control.ToggleButton
import org.wycliffeassociates.otter.jvm.controls.skins.button.SelectButtonSkin
import tornadofx.*

class SelectButton : ToggleButton() {
    init {
        importStylesheet(javaClass.getResource("/css/select-button.css").toExternalForm())
        styleClass.setAll("select-button")
    }

    override fun createDefaultSkin(): Skin<*> {
        return SelectButtonSkin(this)
    }
}
