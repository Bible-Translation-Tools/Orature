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
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.framesToTimecode
import tornadofx.*
import java.text.MessageFormat

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
    private lateinit var menuButton: MenuButton

    init {
        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

        playerProperty.onChange {
            audioSampleRate.set(it?.getAudioReader()?.sampleRate ?: 0)
        }

        alignment = Pos.CENTER
        spacing = 10.0
        button {
            addClass("btn", "btn--icon")
            textProperty().bind(playPauseTextBinding())
            tooltip {
                textProperty().bind(playPauseTextBinding())
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

        menubutton {
            addClass("wa-menu-button")
            graphic = FontIcon(MaterialDesign.MDI_SPEEDOMETER)

            menuButton = this
            popupSideProperty().bind(menuSideProperty)

            items.bind(menuItems) { it }
            textProperty().bind(audioPlaybackRateProperty.stringBinding {
                String.format("%.2fx", it)
            })

            setOnMouseClicked {
                menuItems.setAll(createPlaybackRateMenu())
                show()
            }

            visibleProperty().bind(enablePlaybackRateProperty)
            managedProperty().bind(visibleProperty())
        }

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
                        textProperty().bind(playPauseTextBinding())
                    }
                    graphicProperty().bind(
                        audioPlayerController.isPlayingProperty.objectBinding { isPlaying ->
                            when (isPlaying) {
                                true -> customPauseIcon
                                else -> customPlayIcon
                            }
                        }
                    )
                    action {
                        audioPlayerController.toggle()
                    }
                }
            }
        }

        audioPlaybackRateProperty.onChange {
            audioPlayerController.setPlaybackRate(it)
        }

        menuItems.setAll(createPlaybackRateMenu())
    }

    private fun playPauseTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                when (audioPlayerController.isPlayingProperty.value) {
                    true -> pauseTextProperty.value
                    else -> playTextProperty.value
                }
            },
            audioPlayerController.isPlayingProperty,
            playTextProperty
        )
    }

    private fun createPlaybackRateMenu(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        items.add(
            CustomMenuItem().apply {
                vbox {
                    prefWidth = 400.0
                    spacing = 10.0
                    hbox {
                        styleClass.add("wa-menu-button__separator")
                        alignment = Pos.CENTER
                        label {
                            text = FX.messages["playbackSpeed"]
                        }
                        region {
                            hgrow = Priority.ALWAYS
                        }
                        button {
                            addClass("btn", "btn--secondary", "btn--borderless")
                            text = FX.messages["custom"]
                            action {
                                menuButton.hide()
                                menuItems.setAll(createCustomRateMenu())
                                menuButton.show()
                            }
                        }
                    }
                }
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
                vbox {
                    prefWidth = 400.0
                    spacing = 10.0
                    hbox {
                        styleClass.add("wa-menu-button__separator")
                        alignment = Pos.CENTER
                        label {
                            text = FX.messages["custom"]
                        }
                        region {
                            hgrow = Priority.ALWAYS
                        }
                        button {
                            addClass("btn", "btn--secondary", "btn--borderless")
                            text = FX.messages["cancel"]
                            action {
                                menuItems.setAll(createPlaybackRateMenu())
                            }
                        }
                    }
                    add(JFXSlider().apply {
                        addClass("wa-slider")

                        rateSlider = this
                        min = 0.25
                        max = 2.0

                        value = audioPlaybackRateProperty.value

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
                            menuButton.hide()
                        }
                        fitToParentWidth()
                    }
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
            val title = when (isCustom) {
                true -> MessageFormat.format(
                    FX.messages["customSpeedRate"],
                    FX.messages["custom"],
                    formattedValue
                )
                else ->formattedValue
            }

            content = HBox().apply {
                addClass("wa-menu-button__list-item")
                label {
                    hgrow = Priority.ALWAYS
                    text = title

                }
                tooltip(title)
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
