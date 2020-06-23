package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.*
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceContentSkin

class SourceContent : Control() {

    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val sourceAudioAvailableProperty: BooleanBinding = audioPlayerProperty.isNotNull

    val sourceTextProperty = SimpleStringProperty()
    val sourceTextWidthProperty = SimpleDoubleProperty(Double.MAX_VALUE)
    val sourceFormatChangedProperty = SimpleBooleanProperty(false)

    val sourceAudioLabelProperty = SimpleStringProperty("Source Audio")
    val sourceTextLabelProperty = SimpleStringProperty("Source Text")

    val applyRoundedStyleProperty = SimpleBooleanProperty(false)

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