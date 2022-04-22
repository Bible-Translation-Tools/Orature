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

import com.jfoenix.controls.JFXSlider
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.NodeOrientation
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.Button
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.MenuItem
import javafx.scene.control.Slider
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.button.WaMenuButton
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.framesToTimecode
import tornadofx.*
import java.text.MessageFormat

enum class PlaybackRateType {
    SOURCE,
    TARGET
}

class PlaybackRateChangedEvent(val type: PlaybackRateType, val rate: Double) : FXEvent()

class SimpleAudioPlayer(
    player: IAudioPlayer? = null
) : HBox() {
    val playerProperty = SimpleObjectProperty<IAudioPlayer>(player)
    val playButtonProperty = SimpleObjectProperty<Button>()
    val enablePlaybackRateProperty = SimpleBooleanProperty()
    val audioPlaybackRateProperty = SimpleDoubleProperty(1.0)
    val menuSideProperty = SimpleObjectProperty<Side>()

    val playTextProperty = SimpleStringProperty()
    val pauseTextProperty = SimpleStringProperty()
    var onPlaybackProgressChanged: (value: Double) -> Unit = {}

    private val slider = JFXSlider()
    private val audioPlayerController = AudioPlayerController(slider)

    private val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
    private val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
    private val customPlayIcon = FontIcon(MaterialDesign.MDI_PLAY)
    private val customPauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
    private val audioSampleRate = SimpleIntegerProperty(0)
    private val playbackRateOptions = observableListOf(0.25, 0.50, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0)
    private var customRateProperty = SimpleDoubleProperty(1.0)
    private val menuItems: ObservableList<MenuItem> = observableListOf()

    private lateinit var rateSlider: Slider
    private val menuButton = WaMenuButton()

    init {
        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

        playerProperty.onChange {
            audioSampleRate.set(it?.getAudioReader()?.sampleRate ?: DEFAULT_SAMPLE_RATE)
        }

        alignment = Pos.CENTER
        spacing = 10.0
        button {
            addClass("btn", "btn--icon")
            textProperty().bind(playPauseTextBinding())
            tooltip {
                textProperty().bind(playPauseTextBinding(true))
            }
            graphicProperty().bind(
                audioPlayerController.isPlayingProperty.objectBinding { isPlaying ->
                    if (isPlaying == true) pauseIcon else playIcon
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
                    valueProperty().stringBinding {
                        it?.let {
                            onPlaybackProgressChanged(it.toDouble())
                            framesToTimecode(it.toDouble(), audioSampleRate.value)
                        }
                    }
                }
            }
        )

        add(
            menuButton.apply {
                buttonTextProperty.bind(audioPlaybackRateProperty.stringBinding {
                    String.format("%.2fx", it)
                })
                sideProperty.bind(menuSideProperty)
                items.bind(menuItems) { it }

                visibleProperty().bind(enablePlaybackRateProperty)
                managedProperty().bind(visibleProperty())
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
                    textProperty().bind(playPauseTextBinding())
                    tooltip {
                        textProperty().bind(playPauseTextBinding(true))
                    }
                    graphicProperty().bind(
                        audioPlayerController.isPlayingProperty.objectBinding { isPlaying ->
                            if (isPlaying == true) customPauseIcon else customPlayIcon
                        }
                    )
                    action {
                        audioPlayerController.toggle()
                    }
                }
            }
        }

        audioPlayerController.playbackRateProperty.bind(audioPlaybackRateProperty)

        menuItems.setAll(createPlaybackRateMenu())
    }

    private fun playPauseTextBinding(hideButtonText: Boolean = false): StringBinding {
        return Bindings.createStringBinding(
            {
                val isPlaying = audioPlayerController.isPlayingProperty.value ?: false
                var pauseText = pauseTextProperty.value
                var playText = playTextProperty.value

                if (hideButtonText) {
                    pauseText = pauseText ?: FX.messages["pause"]
                    playText = playText ?: FX.messages["play"]
                }

                if (isPlaying) pauseText else playText
            },
            audioPlayerController.isPlayingProperty,
            playTextProperty
        )
    }

    private fun createPlaybackRateMenu(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        items.add(
            CustomMenuItem().apply {
                styleClass.add("wa-menu-button__separator")
                vbox {
                    prefWidth = 400.0
                    spacing = 10.0
                    hbox {
                        alignment = Pos.CENTER
                        label {
                            text = FX.messages["playbackSpeed"]
                        }
                        region {
                            hgrow = Priority.ALWAYS
                        }
                        button {
                            addClass("btn", "btn--secondary", "btn--borderless", "wa-menu-button__btn-util")
                            text = FX.messages["custom"]
                            action {
                                menuItems.setAll(createCustomRateMenu())
                                menuButton.show()
                            }
                        }
                    }
                }
                action {
                    menuItems.setAll(createCustomRateMenu())
                    menuButton.show()
                }
                isHideOnClick = false
            }
        )

        items.addAll(playbackRateMenuItems())

        customRateProperty.value.let { speed ->
            if (playbackRateOptions.contains(speed).not()) {
                items.add(
                    createPlaybackSpeedItem(speed, true) {
                        audioPlaybackRateProperty.set(speed)
                    }
                )
            }
        }

        return items
    }

    private fun createCustomRateMenu(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        items.add(
            CustomMenuItem().apply {
                styleClass.add("wa-menu-button__separator")
                vbox {
                    prefWidth = 400.0
                    spacing = 10.0
                    hbox {
                        alignment = Pos.CENTER
                        label {
                            text = FX.messages["custom"]
                        }
                        region {
                            hgrow = Priority.ALWAYS
                        }
                        button {
                            addClass("btn", "btn--secondary", "btn--borderless", "wa-menu-button__btn-util")
                            text = FX.messages["cancel"]
                            action {
                                menuItems.setAll(createPlaybackRateMenu())
                                menuButton.show()
                            }
                        }
                    }
                    add(JFXSlider().apply {
                        addClass("wa-slider")

                        rateSlider = this
                        min = playbackRateOptions.first()
                        max = playbackRateOptions.last()

                        value = audioPlaybackRateProperty.value
                        blockIncrement = (10 * max) / 100 // 10% intervals

                        setValueFactory {
                            Bindings.createStringBinding(
                                {
                                    String.format("%.2fx", it.value)
                                },
                                valueProperty()
                            )
                        }
                    })
                    label {
                        addClass("wa-menu-button__large-text")
                        alignment = Pos.CENTER
                        textProperty().bind(rateSlider.valueProperty().stringBinding {
                            String.format("%.2fx", it)
                        })
                        fitToParentWidth()
                    }
                    button {
                        addClass("btn", "btn--secondary")
                        hgrow = Priority.ALWAYS
                        alignment = Pos.CENTER
                        text = FX.messages["setCustom"]
                        action {
                            customRateProperty.set(rateSlider.value)
                            audioPlaybackRateProperty.set(rateSlider.value)
                            menuItems.setAll(createPlaybackRateMenu())
                            menuButton.hide()
                        }
                        fitToParentWidth()
                    }
                }
                action {
                    menuItems.setAll(createPlaybackRateMenu())
                    menuButton.show()
                }
                isHideOnClick = false
            }
        )
        return items
    }

    private fun playbackRateMenuItems(): List<MenuItem> {
        return playbackRateOptions.map { option ->
            createPlaybackSpeedItem(option) {
                audioPlaybackRateProperty.set(option)
            }
        }
    }

    private fun createPlaybackSpeedItem(
        speed: Double,
        isCustom: Boolean = false,
        onSelected: () -> Unit
    ): MenuItem {
        return CustomMenuItem().apply {
            val formattedValue = String.format("%.2fx", speed)
            val title = if (isCustom) {
                MessageFormat.format(
                    FX.messages["customSpeedRate"],
                    FX.messages["custom"],
                    formattedValue
                )
            } else formattedValue

            content = HBox().apply {
                addClass("wa-menu-button__list-item")
                label {
                    hgrow = Priority.ALWAYS
                    text = title
                }
                tooltip(title)

                setOnKeyReleased {
                    when (it.code) {
                        KeyCode.ENTER, KeyCode.SPACE -> {
                            onSelected()
                            menuButton.hide()
                        }
                    }
                }
            }
            setOnAction {
                onSelected()
            }
        }
    }
}

fun EventTarget.simpleaudioplayer(
    player: IAudioPlayer? = null,
    op: SimpleAudioPlayer.() -> Unit = {}
) = SimpleAudioPlayer(player).attachTo(this, op)
