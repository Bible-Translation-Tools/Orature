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
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.geometry.NodeOrientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.framesToTimecode
import tornadofx.*
import tornadofx.FX.Companion.messages

class SimpleAudioPlayer(
    player: IAudioPlayer? = null
) : HBox() {
    val playerProperty = SimpleObjectProperty<IAudioPlayer>(player)
    val playButtonProperty = SimpleObjectProperty<Button>()

    private val slider = JFXSlider()
    private val audioPlayerController = AudioPlayerController(slider)

    private val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
    private val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
    private val audioSampleRate = SimpleIntegerProperty(0)

    init {
        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

        playerProperty.onChange {
            audioSampleRate.set(it?.getAudioReader()?.sampleRate ?: 0)
        }

        alignment = Pos.CENTER
        spacing = 10.0
        button {
            addClass("btn", "btn--icon")
            tooltip {
                textProperty().bind(audioPlayerController.isPlayingProperty.stringBinding {
                    if (it == true) messages["pause"] else messages["play"]
                })
            }
            graphicProperty().bind(
                audioPlayerController.isPlayingProperty.objectBinding { isPlaying ->
                    when (isPlaying) {
                        true -> pauseIcon
                        else -> playIcon
                    }
                }
            )
            action {
                audioPlayerController.toggle()
            }
            visibleProperty().bind(playButtonProperty.isNull)
            managedProperty().bind(visibleProperty())
        }
        add(
            slider.apply {
                addClass("wa-slider")
                hgrow = Priority.ALWAYS
                value = 0.0

                setValueFactory {
                    Bindings.createStringBinding(
                        {
                            framesToTimecode(it.value, audioSampleRate.value)
                        },
                        valueProperty()
                    )
                }
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

        playButtonProperty.onChange {
            it?.let { button ->
                button.apply {
                    tooltip {
                        textProperty().bind(audioPlayerController.isPlayingProperty.stringBinding {
                            if (it == true) messages["pause"] else messages["play"]
                        })
                    }
                    graphicProperty().bind(
                        audioPlayerController.isPlayingProperty.objectBinding { isPlaying ->
                            when (isPlaying) {
                                true -> pauseIcon
                                else -> playIcon
                            }
                        }
                    )
                    action {
                        audioPlayerController.toggle()
                    }
                }
            }
        }
    }
}

fun EventTarget.simpleaudioplayer(
    player: IAudioPlayer? = null,
    op: SimpleAudioPlayer.() -> Unit = {}
) = SimpleAudioPlayer(player).attachTo(this, op)
