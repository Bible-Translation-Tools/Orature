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

import com.jfoenix.controls.JFXSlider
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import tornadofx.*
import java.io.File

class SimpleAudioPlayer(
    file: File? = null,
    player: IAudioPlayer? = null
) : HBox() {
    val fileProperty = SimpleObjectProperty<File>(file)
    val playerProperty = SimpleObjectProperty<IAudioPlayer>(player)

    private val slider = JFXSlider()
    private val audioPlayerController = AudioPlayerController(slider)

    private val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
    private val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)

    init {
        alignment = Pos.CENTER
        spacing = 10.0
        button {
            addClass("btn", "btn--icon")
            graphicProperty().bind(
                audioPlayerController.isPlayingProperty.objectBinding {
                    when (it) {
                        true -> pauseIcon
                        else -> playIcon
                    }
                }
            )
            action {
                audioPlayerController.toggle()
            }
        }
        add(
            slider.apply {
                addClass("wa-slider")
                hgrow = Priority.ALWAYS
                value = 0.0
            }
        )

        initController()
    }

    private fun initController() {
        playerProperty.onChange {
            it?.let {
                audioPlayerController.load(it)
            }
        }

        fileProperty.onChange {
            it?.let { file ->
                playerProperty.value?.let { player ->
                    player.load(file)
                    audioPlayerController.load(player)
                }
            }
        }
    }
}

fun EventTarget.simpleaudioplayer(
    file: File? = null,
    player: IAudioPlayer? = null,
    op: SimpleAudioPlayer.() -> Unit = {}
) = SimpleAudioPlayer(file, player).attachTo(this, op)
