/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu

import javafx.beans.property.BooleanProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.NarrationStateType
import org.wycliffeassociates.otter.jvm.controls.event.OpenInAudioPluginEvent
import org.wycliffeassociates.otter.jvm.controls.event.PlayVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordAgainEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class VerseMenu : ContextMenu() {

    val verseProperty = SimpleObjectProperty<AudioMarker>()
    val verseIndexProperty = SimpleIntegerProperty()

    val narrationStateProperty = SimpleObjectProperty<NarrationStateType>()

    init {
        addClass("wa-context-menu")

        val playOpt = MenuItem().apply {
            graphic = label(messages["play"]) {
                graphic = FontIcon(MaterialDesign.MDI_PLAY)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(PlayVerseEvent(verseProperty.value))
            }
            disableWhen {
                narrationStateProperty.isEqualTo(NarrationStateType.RECORDING)
                    .or(narrationStateProperty.isEqualTo(NarrationStateType.RECORDING_AGAIN))
            }
        }
        val recordAgainOpt = MenuItem().apply {
            graphic = label(messages["recordAgain"]) {
                graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(RecordAgainEvent(verseIndexProperty.value))
            }
            disableWhen {
                narrationStateProperty.isEqualTo(NarrationStateType.RECORDING)
                    .or(narrationStateProperty.isEqualTo(NarrationStateType.RECORDING_AGAIN))
            }
        }
        val editVerseOpt = MenuItem().apply {
            graphic = label(messages["openIn"]) {
                graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(OpenInAudioPluginEvent(verseIndexProperty.value))
            }
            disableWhen {
                narrationStateProperty.isEqualTo(NarrationStateType.RECORDING)
                    .or(narrationStateProperty.isEqualTo(NarrationStateType.RECORDING_AGAIN))
            }
        }

        items.setAll(playOpt, recordAgainOpt, editVerseOpt)
    }
}
