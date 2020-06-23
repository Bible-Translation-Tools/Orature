package org.wycliffeassociates.otter.jvm.controls.sourceformattoggle

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceFormatToggleSkin

class SourceFormatToggle: Control() {

    private val USER_AGENT_STYLESHEET = javaClass.getResource("/css/source-format-toggle.css").toExternalForm()

    val displayPlayerProperty = SimpleBooleanProperty(true)

    init {
        initialize()
    }

    override fun getUserAgentStylesheet(): String {
        return USER_AGENT_STYLESHEET
    }

    override fun createDefaultSkin(): Skin<*> {
        return SourceFormatToggleSkin(this)
    }

    private fun initialize() {
        stylesheets.setAll(USER_AGENT_STYLESHEET)
    }
}