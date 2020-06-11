package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.media.AudioPlayerSkin

class AudioPlayerNode(private var player: IAudioPlayer?) : Control() {

    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>(player)
    val sourceTextProperty = SimpleStringProperty()
    val sourceTextWidthProperty = SimpleDoubleProperty(Double.MAX_VALUE)
    val refreshParentProperty = SimpleBooleanProperty(false)
    val sourceAudioLabelProperty = SimpleStringProperty("Source Audio")

    val sourceAvailable: Boolean
        get() = player != null

    fun load(player: IAudioPlayer) {
        this.player = player
        audioPlayerProperty.set(player)
    }

    override fun createDefaultSkin(): Skin<*> {
        return AudioPlayerSkin(this)
    }
}