package org.wycliffeassociates.otter.jvm.controls.sourcecontent

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceContentSkin
import org.wycliffeassociates.otter.jvm.controls.sourceformattoggle.SourceFormatToggle

class SourceContent : Control() {
    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val sourceAudioAvailableProperty: BooleanBinding = audioPlayerProperty.isNotNull

    val sourceTextProperty = SimpleStringProperty()
    val activeSourceFormatProperty = SimpleObjectProperty<SourceFormatToggle.SourceFormat>()

    val sourceAudioLabelProperty = SimpleStringProperty("Source Audio")
    val sourceTextLabelProperty = SimpleStringProperty("Source Text")

    private val USER_AGENT_STYLESHEET = javaClass.getResource("/css/source-content.css").toExternalForm()

    init {
        initialize()
    }

    override fun createDefaultSkin(): Skin<*> {
        return SourceContentSkin(this)
    }

    override fun getUserAgentStylesheet(): String {
        return USER_AGENT_STYLESHEET
    }

    private fun initialize() {
        stylesheets.setAll(USER_AGENT_STYLESHEET)
    }
}