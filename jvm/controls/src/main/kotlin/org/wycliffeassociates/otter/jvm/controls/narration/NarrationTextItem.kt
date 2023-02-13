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
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class NarrationTextItem : VBox() {
    val verseLabelProperty = SimpleStringProperty()
    val verseTextProperty = SimpleStringProperty()
    val isActiveProperty = SimpleBooleanProperty()
    val isLastVerseProperty = SimpleBooleanProperty()

    val beginRecordingTextProperty = SimpleStringProperty()
    val pauseRecordingTextProperty = SimpleStringProperty()
    val resumeRecordingTextProperty = SimpleStringProperty()
    val nextChunkTextProperty = SimpleStringProperty()

    val onRecordActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onNextVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("narration-list__verse-item")

        hbox {
            addClass("narration-list__verse-block")
            label(verseLabelProperty) {
                addClass("narration-list__verse-item-text", "narration-list__verse-item-text__title")
                translateY -= 5.0
            }
            label(verseTextProperty).apply {
                addClass("narration-list__verse-item-text")
                isWrapText = true

                prefWidthProperty().bind(this@NarrationTextItem.maxWidthProperty().subtract(50))
            }

        }
        separator {
            addClass("narration-list__separator")
            visibleProperty().bind(isActiveProperty)
            managedProperty().bind(visibleProperty())
        }
        hbox {
            addClass("narration-list__buttons")
            alignment = Pos.BASELINE_LEFT

            button(beginRecordingTextProperty) {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)

                onActionProperty().bind(onRecordActionProperty)
            }
            button(nextChunkTextProperty) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_ARROW_DOWN)

                onActionProperty().bind(onNextVerseActionProperty)
                disableProperty().bind(isLastVerseProperty)
            }

            visibleProperty().bind(isActiveProperty)
            managedProperty().bind(visibleProperty())
        }

        disableProperty().bind(isActiveProperty.not())
    }

    fun setOnRecord(op: () -> Unit) {
        onRecordActionProperty.set(EventHandler {
            op.invoke()
        })
    }

    fun setOnNextVerse(op: () -> Unit) {
        onNextVerseActionProperty.set(
            EventHandler {
                op.invoke()
            }
        )
    }
}