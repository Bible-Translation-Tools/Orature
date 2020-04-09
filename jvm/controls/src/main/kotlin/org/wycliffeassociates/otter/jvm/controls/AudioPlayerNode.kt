package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.media.AudioPlayerSkin

class AudioPlayerNode(private var player: IAudioPlayer?) : Control() {

    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>(player)

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