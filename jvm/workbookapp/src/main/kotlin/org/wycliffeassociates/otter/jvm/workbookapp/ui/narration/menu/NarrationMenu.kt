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

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableObjectValue
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.NarrationStateType
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import tornadofx.*
import tornadofx.FX.Companion.messages

class NarrationMenu : ContextMenu() {

    val narrationStateProperty = SimpleObjectProperty<NarrationStateType>()

    init {
        addClass("wa-context-menu")
        isAutoHide = true

        val openChapterOpt = MenuItem().apply {
            addClass("btn", "btn--tertiary", "btn--borderless")
            graphic = label(messages["openChapterIn"]) {
                graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(NarrationOpenInPluginEvent(PluginType.EDITOR))
            }
        }
        val verseMarkerOpt = MenuItem().apply {
            addClass("btn", "btn--tertiary", "btn--borderless")
            graphic = label(messages["editVerseMarkers"]) {
                graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(NarrationOpenInPluginEvent(PluginType.MARKER))
            }
            enableWhen(
                narrationStateProperty.isEqualTo(NarrationStateType.IN_PROGRESS)
                    .or(narrationStateProperty.isEqualTo(NarrationStateType.FINISHED))
            )
        }
        val restartChapterOpt = MenuItem().apply {
            addClass("btn", "btn--tertiary", "btn--borderless")
            graphic = label(messages["restartChapter"]) {
                graphic = FontIcon(MaterialDesign.MDI_DELETE)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(NarrationRestartChapterEvent())
            }
            enableWhen(
                narrationStateProperty.isEqualTo(NarrationStateType.FINISHED)
                    .or(narrationStateProperty.isEqualTo(NarrationStateType.IN_PROGRESS))
            )
        }

        val importChapterAudio = MenuItem().apply {
            addClass("btn", "btn--tertiary", "btn--borderless")
            graphic = label(messages["import"]) {
                graphic = FontIcon(MaterialDesign.MDI_DOWNLOAD)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(NarrationOpenImportAudioDialogEvent())
            }
            disableWhen {
                narrationStateProperty.isEqualTo(NarrationStateType.RECORDING)
                    .or(narrationStateProperty.isEqualTo(NarrationStateType.RECORDING_AGAIN))
                    .or(narrationStateProperty.isEqualTo(NarrationStateType.PLAYING))
            }
        }

        items.setAll(openChapterOpt, verseMarkerOpt, restartChapterOpt, importChapterAudio)
    }
}

fun EventTarget.narrationMenuButton(
    narrationStateBinding: ObservableObjectValue<NarrationStateType>,
    op: Button.() -> Unit = {}
): Button {
    return Button().attachTo(this).apply {
        addClass("btn", "btn--icon", "btn--tertiary")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)
        tooltip(messages["options"])

        val menu = NarrationMenu().apply {
            this.narrationStateProperty.bind(narrationStateBinding)
        }

        menu.setOnShowing { addPseudoClass("active") }
        menu.setOnHidden { removePseudoClass("active") }

        action {
            val screenBound = localToScreen(boundsInLocal)
            menu.show(FX.primaryStage)
            menu.x = screenBound.centerX - menu.width + this.width
            menu.y = screenBound.minY + this.height - 5.0
        }
        op()
    }
}