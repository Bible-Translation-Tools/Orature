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
package org.wycliffeassociates.otter.jvm.controls.dialog

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.NodeOrientation
import javafx.geometry.Pos
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import tornadofx.*

class PluginOpenedPage : View() {

    val dialogTitleProperty = SimpleStringProperty()
    val dialogTextProperty = SimpleStringProperty()
    val licenseProperty = SimpleStringProperty()
    val playerProperty = SimpleObjectProperty<IAudioPlayer>()
    val targetAudioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val audioAvailableProperty = SimpleBooleanProperty(false)
    val sourceTextProperty = SimpleStringProperty()
    val sourceContentTitleProperty = SimpleStringProperty()
    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val sourceOrientationProperty = SimpleObjectProperty<NodeOrientation>()
    val sourceSpeedRateProperty = SimpleDoubleProperty()
    val targetSpeedRateProperty = SimpleDoubleProperty()
    val sourceTextZoomRateProperty = SimpleIntegerProperty()

    private val playerListenerDisposers = mutableListOf<ListenerDisposer>()

    init {
        tryImportStylesheet(resources["/css/plugin-opened-page.css"])
    }

    override val root = vbox {
        alignment = Pos.CENTER
        addClass("plugin-opened-page")
        label(dialogTitleProperty) {
            addClass("plugin-opened-page__title", "plugin-opened-page__label")
            visibleWhen(textProperty().isNotEmpty)
            managedProperty().bind(visibleProperty())
        }
        label(dialogTextProperty) {
            alignment = Pos.CENTER
            addClass("plugin-opened-page__label", "plugin-opened-page__label--message")
            visibleWhen(textProperty().isNotEmpty)
            managedWhen(visibleProperty())
        }
        add(
            SourceContent().apply {
                addClass("plugin-opened-page__source")
                vgrow = Priority.ALWAYS
                sourceTextProperty.bind(this@PluginOpenedPage.sourceTextProperty)
                sourceAudioPlayerProperty.bind(playerProperty)
                targetAudioPlayerProperty.bind(this@PluginOpenedPage.targetAudioPlayerProperty)
                licenseProperty.bind(this@PluginOpenedPage.licenseProperty)

                audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
                textNotAvailableTextProperty.set(messages["textNotAvailable"])
                playSourceLabelProperty.set(messages["playSource"])
                pauseSourceLabelProperty.set(messages["pauseSource"])
                playTargetLabelProperty.set(messages["playTarget"])
                pauseTargetLabelProperty.set(messages["pauseTarget"])

                orientationProperty.bind(this@PluginOpenedPage.orientationProperty)
                sourceOrientationProperty.bind(this@PluginOpenedPage.sourceOrientationProperty)
                contentTitleProperty.bind(sourceContentTitleProperty)
                isMinimizableProperty.set(false)

                sourceSpeedRateProperty.bind(this@PluginOpenedPage.sourceSpeedRateProperty)
                targetSpeedRateProperty.bind(this@PluginOpenedPage.targetSpeedRateProperty)
                this@PluginOpenedPage.sourceTextZoomRateProperty.onChange {
                    zoomRateProperty.set(it)
                }
            }
        )
    }

    override fun onDock() {
        playerProperty.onChangeAndDoNowWithDisposer {
            it?.let {
                addShortcut(Shortcut.PLAY_SOURCE.value, it::toggle)
            }
        }.let(playerListenerDisposers::add)

        targetAudioPlayerProperty.onChangeAndDoNowWithDisposer {
            it?.let {
                addShortcut(Shortcut.PLAY_TARGET.value, it::toggle)
            }
        }.let(playerListenerDisposers::add)
        super.onDock()
    }

    override fun onUndock() {
        playerProperty.value?.stop()
        targetAudioPlayerProperty.value?.close()
        playerListenerDisposers.forEach(ListenerDisposer::dispose)
        playerListenerDisposers.clear()
        removeShortcut(Shortcut.PLAY_SOURCE.value)
        removeShortcut(Shortcut.PLAY_TARGET.value)
        super.onUndock()
    }

    private fun addShortcut(combo: KeyCodeCombination, action: () -> Unit) {
        workspace.shortcut(combo, action)
    }

    private fun removeShortcut(combo: KeyCodeCombination) {
        workspace.accelerators.remove(combo)
    }
}
