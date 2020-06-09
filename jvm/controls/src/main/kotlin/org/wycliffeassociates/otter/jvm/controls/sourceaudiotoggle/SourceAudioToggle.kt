package org.wycliffeassociates.otter.jvm.controls.sourceaudiotoggle

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceAudioToggleSkin

class SourceAudioToggle: Control() {

    val displayPlayerProperty = SimpleBooleanProperty(true)

    override fun getUserAgentStylesheet(): String {
        return this.javaClass.getResource("/css/source-audio-toggle.css").toExternalForm()
    }

    override fun createDefaultSkin(): Skin<*> {
        return SourceAudioToggleSkin(this)
    }
}