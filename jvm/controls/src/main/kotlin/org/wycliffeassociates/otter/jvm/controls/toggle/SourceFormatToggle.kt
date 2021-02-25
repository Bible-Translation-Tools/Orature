package org.wycliffeassociates.otter.jvm.controls.toggle

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceFormatToggleSkin

class SourceFormatToggle : Control() {

    enum class SourceFormat {
        AUDIO,
        TEXT
    }

    private val USER_AGENT_STYLESHEET = javaClass.getResource("/css/source-format-toggle.css").toExternalForm()

    val activeSourceProperty = SimpleObjectProperty<SourceFormat>(SourceFormat.AUDIO)

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
