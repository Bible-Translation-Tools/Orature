/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

    val targetAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()

    val sourceTextProperty = SimpleStringProperty()
    val sourceTextAvailableProperty: BooleanBinding = sourceTextProperty.isNotNull

    val audioNotAvailableTextProperty = SimpleStringProperty()
    val textNotAvailableTextProperty = SimpleStringProperty()

    val playLabelProperty = SimpleStringProperty()
    val pauseLabelProperty = SimpleStringProperty()

    val playTargetLabelProperty = SimpleStringProperty()
    val pauseTargetLabelProperty = SimpleStringProperty()

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
