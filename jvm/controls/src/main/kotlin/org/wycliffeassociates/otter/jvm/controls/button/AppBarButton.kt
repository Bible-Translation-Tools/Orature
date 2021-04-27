package org.wycliffeassociates.otter.jvm.controls.button

import javafx.scene.control.Skin
import javafx.scene.control.ToggleButton
import org.wycliffeassociates.otter.jvm.controls.skins.button.AppBarButtonSkin

class AppBarButton : ToggleButton() {

    init {
        styleClass.setAll("app-bar-button")
    }

    override fun createDefaultSkin(): Skin<*> {
        return AppBarButtonSkin(this)
    }
}
