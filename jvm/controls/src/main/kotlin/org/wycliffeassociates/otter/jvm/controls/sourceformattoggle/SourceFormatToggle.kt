package org.wycliffeassociates.otter.jvm.controls.sourceformattoggle

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceFormatToggleSkin

class SourceFormatToggle: Control() {

    val displayPlayerProperty = SimpleBooleanProperty(true)

    override fun getUserAgentStylesheet(): String {
        return this.javaClass.getResource("/css/source-format-toggle.css").toExternalForm()
    }

    override fun createDefaultSkin(): Skin<*> {
        return SourceFormatToggleSkin(this)
    }
}