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

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.NodeOrientation
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceContentSkin
import tornadofx.*
import java.text.MessageFormat

class SourceTextZoomRateChangedEvent(val rate: Int) : FXEvent()

class SourceContent : Control() {
    val contentTitleProperty = SimpleStringProperty()

    val sourceAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val sourceAudioAvailableProperty: BooleanBinding = sourceAudioPlayerProperty.isNotNull
    val sourceSpeedRateProperty = SimpleDoubleProperty()
    val targetAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val targetSpeedRateProperty = SimpleDoubleProperty()

    val sourceTextProperty = SimpleStringProperty()
    val sourceTextAvailableProperty: BooleanBinding = sourceTextProperty.isNotNull
    val sourceTextChunks = observableListOf<String>()
    val highlightedChunk = SimpleIntegerProperty(-1)

    val licenseProperty = SimpleStringProperty()
    val licenseTextProperty = SimpleStringProperty()

    val audioNotAvailableTextProperty = SimpleStringProperty()
    val textNotAvailableTextProperty = SimpleStringProperty()

    val playSourceLabelProperty = SimpleStringProperty()
    val pauseSourceLabelProperty = SimpleStringProperty()

    val playTargetLabelProperty = SimpleStringProperty()
    val pauseTargetLabelProperty = SimpleStringProperty()

    val enableAudioProperty = SimpleBooleanProperty(true)
    val isMinimizableProperty = SimpleBooleanProperty(true)
    val isMinimizedProperty = SimpleBooleanProperty(false)
    val zoomRateProperty = SimpleIntegerProperty(100)

    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val sourceOrientationProperty = SimpleObjectProperty<NodeOrientation>()

    private val userAgentStyleSheet = javaClass.getResource("/css/source-content.css").toExternalForm()

    init {
        addClass("source-content")

        initialize()
        licenseProperty.onChange {
            licenseTextProperty.set(
                MessageFormat.format(FX.messages["licenseStatement"], it)
            )
        }
        sourceTextProperty.onChange {
            val chunks = it?.split("\n") ?: listOf()
            sourceTextChunks.setAll(chunks)
        }
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
