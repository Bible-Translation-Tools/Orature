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

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import tornadofx.*

class NarrationRecordItem : VBox() {
    val verseLabelProperty = SimpleStringProperty()
    val waveformProperty = SimpleObjectProperty<Image>()
    val waveformLoadingProperty = SimpleBooleanProperty()
    val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()

    val openInTextProperty = SimpleStringProperty()
    val recordAgainTextProperty = SimpleStringProperty()
    val loadingImageTextProperty = SimpleStringProperty()

    val onOpenAppActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordAgainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val audioPlayButtonProperty = SimpleObjectProperty<Button>()

    init {
        styleClass.setAll("narration-record__verse-item")

        hbox {
            addClass("narration-record__verse-controls")

            label {
                addClass("narration-record__verse-text")

                graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                textProperty().bind(verseLabelProperty)
            }
            region {
                hgrow = Priority.ALWAYS
            }
            button {
                addClass("btn", "btn--primary", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_PLAY)
                audioPlayButtonProperty.set(this)
            }

            simpleaudioplayer {
                playerProperty.bind(audioPlayerProperty)
                playButtonProperty.bind(audioPlayButtonProperty)
                isVisible = false
                isManaged = false
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
            alignment = Pos.CENTER
            vgrow = Priority.ALWAYS

            hbox {
                addClass("narration-record__waveform")
                imageview(waveformProperty)
            }

            label(loadingImageTextProperty) {
                visibleProperty().bind(waveformLoadingProperty)
            }
        }
    }
}
