package org.wycliffeassociates.otter.jvm.controls.media

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceContentSkin

class SourceContent : Control() {
    val contentTitleProperty = SimpleStringProperty()

    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val sourceAudioAvailableProperty: BooleanBinding = audioPlayerProperty.isNotNull

    val sourceTextProperty = SimpleStringProperty()
    val sourceTextAvailableProperty: BooleanBinding = sourceTextProperty.isNotNull

    val audioNotAvailableTextProperty = SimpleStringProperty()
    val textNotAvailableTextProperty = SimpleStringProperty()

    val playLabelProperty = SimpleStringProperty()
    val pauseLabelProperty = SimpleStringProperty()

    val enableAudioProperty = SimpleBooleanProperty(true)
    val isMinimizableProperty = SimpleBooleanProperty(true)
    val isMinimizedProperty = SimpleBooleanProperty(false)

    private val userAgentStyleSheet = javaClass.getResource("/css/source-content.css").toExternalForm()

    init {
        initialize()
    }

    override fun createDefaultSkin(): Skin<*> {
        return SourceContentSkin(this)
    }

    override fun getUserAgentStylesheet(): String {
        return userAgentStyleSheet
    }

    private fun initialize() {
        stylesheets.setAll(userAgentStyleSheet)
    }
}
