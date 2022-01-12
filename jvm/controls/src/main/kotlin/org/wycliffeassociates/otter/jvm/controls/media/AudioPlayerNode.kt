/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.controls.media

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.media.AudioPlayerSkin

open class AudioPlayerNode(private var player: IAudioPlayer?) : Control() {

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
