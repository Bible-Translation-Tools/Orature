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
package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.binding.Bindings
import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.NodeOrientation
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import java.text.MessageFormat

class NarrationRecordItem : VBox() {
    val verseLabelProperty = SimpleStringProperty()
    val waveformProperty = SimpleObjectProperty<Image>()
    val invertedWaveformProperty = SimpleObjectProperty<Image>()
    val waveformLoadingProperty = SimpleBooleanProperty()

    val openInTextProperty = SimpleStringProperty()
    val recordAgainTextProperty = SimpleStringProperty()
    val loadingImageTextProperty = SimpleStringProperty()
    val goToVerseTextProperty = SimpleStringProperty()

    val onPlayActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onOpenAppActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordAgainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onWaveformClickActionProperty = SimpleObjectProperty<EventHandler<MouseEvent>>()

    val isRecordingProperty = SimpleBooleanProperty()
    val isPlayingProperty = SimpleBooleanProperty()
    val playbackPositionProperty = SimpleIntegerProperty()
    val totalFramesProperty = SimpleIntegerProperty()
    private val playerWidthProperty = SimpleDoubleProperty()

    private val playIcon = FontIcon(MaterialDesign.MDI_PLAY)
    private val pauseIcon = FontIcon(MaterialDesign.MDI_PAUSE)
    private val cursorWidth = 3.0

    init {
        styleClass.setAll("narration-record__verse-item")

        nodeOrientation = NodeOrientation.LEFT_TO_RIGHT

        playbackPositionProperty.onChange {
            toggleClass("playing", it > 0)
        }

        hbox {
            addClass("narration-record__verse-controls")

            label {
                addClass("narration-record__verse-text")

                graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                textProperty().bind(verseLabelProperty)

                playbackPositionProperty.onChange {
                    toggleClass("playing", it > 0)
                }
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button {
                addClass("btn", "btn--primary", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_PLAY)

                graphicProperty().bind(
                    isPlayingProperty.objectBinding { isPlaying ->
                        if (isPlaying == true) pauseIcon else playIcon
                    }
                )

                onActionProperty().bind(onPlayActionProperty)

                playbackPositionProperty.onChange {
                    toggleClass("playing", it > 0)
                }
            }

            menubutton {
                addClass("btn", "btn--primary", "btn--borderless", "wa-menu-button")
                graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL)

                item("") {
                    textProperty().bind(openInTextProperty)
                    graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                    onActionProperty().bind(onOpenAppActionProperty)
                }
                item("") {
                    textProperty().bind(recordAgainTextProperty)
                    graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                    onActionProperty().bind(onRecordAgainActionProperty)
                }
            }
        }

        stackpane {
            alignment = Pos.CENTER_LEFT
            vgrow = Priority.ALWAYS

            hbox {
                addClass("narration-record__waveform")
                imageview(waveformProperty).apply {
                    visibleProperty().bind(playbackPositionProperty.booleanBinding {
                        it?.let { it.toDouble() <= 0 } ?: true
                    }.and(isRecordingProperty.not()))
                    managedProperty().bind(visibleProperty())
                }
                imageview(invertedWaveformProperty).apply {
                    visibleProperty().bind(playbackPositionProperty.booleanBinding {
                        it?.let { it.toDouble() > 0 } ?: false
                    }.and(isRecordingProperty.not()))
                    managedProperty().bind(visibleProperty())
                }
            }

            region {
                addClass("narration-record__waveform-cursor")
                translateXProperty().bind(playbackPositionBinding())

                // Hide cursor when playback position is less that cursor width
                visibleProperty().bind(playbackPositionBinding().booleanBinding {
                    it?.let { it.toDouble() > cursorWidth } ?: false
                })
            }

            hbox {
                addClass("narration-record__waveform-overlay")

                label(goToVerseTextBinding())

                onMouseClickedProperty().bind(onWaveformClickActionProperty)
                visibleProperty().bind(this@stackpane.hoverProperty())
            }

            label(loadingImageTextProperty) {
                visibleProperty().bind(waveformLoadingProperty)
            }

            playerWidthProperty.bind(widthProperty())
        }
    }

    private fun goToVerseTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            {
                if (goToVerseTextProperty.value != null && verseLabelProperty.value != null) {
                    MessageFormat.format(
                        goToVerseTextProperty.value,
                        verseLabelProperty.value
                    )
                } else ""
            },
            verseLabelProperty,
            goToVerseTextProperty
        )
    }

    private fun playbackPositionBinding(): DoubleBinding {
        return Bindings.createDoubleBinding(
            {
                (playerWidthProperty.value * playbackPositionProperty.value) / totalFramesProperty.value
            },
            playbackPositionProperty,
            totalFramesProperty,
            playerWidthProperty
        )
    }
}
