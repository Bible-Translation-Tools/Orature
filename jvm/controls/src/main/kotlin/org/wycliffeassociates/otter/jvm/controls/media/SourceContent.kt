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

import java.text.MessageFormat
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.media.SourceContentSkin
import tornadofx.FX
import tornadofx.get
import tornadofx.observableListOf
import tornadofx.onChange

class SourceContent : Control() {
    val contentTitleProperty = SimpleStringProperty()

    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val sourceAudioAvailableProperty: BooleanBinding = audioPlayerProperty.isNotNull
    val audioSampleRate = SimpleIntegerProperty(0)

    val targetAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()

    val sourceTextProperty = SimpleStringProperty()
    val sourceTextAvailableProperty: BooleanBinding = sourceTextProperty.isNotNull

    val licenseProperty = SimpleStringProperty()
    val licenseTextProperty = SimpleStringProperty()

    val audioNotAvailableTextProperty = SimpleStringProperty()
    val textNotAvailableTextProperty = SimpleStringProperty()

    val playLabelProperty = SimpleStringProperty()
    val pauseLabelProperty = SimpleStringProperty()

    val playTargetLabelProperty = SimpleStringProperty()
    val pauseTargetLabelProperty = SimpleStringProperty()

    val enableAudioProperty = SimpleBooleanProperty(true)
    val isMinimizableProperty = SimpleBooleanProperty(true)
    val isMinimizedProperty = SimpleBooleanProperty(false)

    val playbackRateOptions = observableListOf("0.25", "0.30", "0.35", "0.40", "0.45", "0.50", "0.55", "0.60", "0.65", "0.70", "0.75", "0.80", "0.85", "0.90", "0.95", "1.0", "1.25", "1.5", "1.75", "2.0")
    val sourceAudioPlaybackRate = SimpleStringProperty("1.0")
    val targetAudioPlaybackRate = SimpleStringProperty("1.0")

    private val userAgentStyleSheet = javaClass.getResource("/css/source-content.css").toExternalForm()

    init {
        initialize()
        audioPlayerProperty.onChange {
            audioSampleRate.set(it?.getAudioReader()?.sampleRate ?: 0)
        }
        licenseProperty.onChange {
            licenseTextProperty.set(
                MessageFormat.format(FX.messages["licenseStatement"], it)
            )
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
