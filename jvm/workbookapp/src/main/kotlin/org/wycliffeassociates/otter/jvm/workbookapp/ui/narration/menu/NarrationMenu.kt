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

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import tornadofx.*
import tornadofx.FX.Companion.messages

class NarrationMenu : ContextMenu() {

    val hasChapterTakeProperty = SimpleBooleanProperty()
    val hasVersesProperty = SimpleBooleanProperty()
    val hasAllChunksRecordedProperty = SimpleBooleanProperty()

    init {
        addClass("wa-context-menu")
        isAutoHide = true

        val openChapterOpt = MenuItem().apply {
            graphic = label(messages["openChapterIn"]) {
                graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(NarrationOpenInPluginEvent(PluginType.EDITOR))
            }
            enableWhen(hasChapterTakeProperty)
        }
        val verseMarkerOpt = MenuItem().apply {
            graphic = label(messages["editVerseMarkers"]) {
                graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(NarrationOpenInPluginEvent(PluginType.MARKER))
            }
            enableWhen(hasChapterTakeProperty.and(hasAllChunksRecordedProperty))
        }
        val restartChapterOpt = MenuItem().apply {
            graphic = label(messages["restartChapter"]) {
                graphic = FontIcon(MaterialDesign.MDI_DELETE)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(NarrationRestartChapterEvent())
            }
            enableWhen(hasVersesProperty)
        }

        items.setAll(openChapterOpt, verseMarkerOpt, restartChapterOpt)
    }
}

fun EventTarget.narrationMenuButton(
    hasChapterTakeBinding: ObservableBooleanValue,
    hasVersesBinding: ObservableBooleanValue,
    hasAllChunksRecordedProperty: ObservableBooleanValue,
    op: Button.() -> Unit = {}
): Button {
    return Button().attachTo(this).apply {
        addClass("btn", "btn--icon")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)
        tooltip(messages["options"])

        val menu = NarrationMenu().apply {
            this.hasChapterTakeProperty.bind(hasChapterTakeBinding)
            this.hasVersesProperty.bind(hasVersesBinding)
            this.hasAllChunksRecordedProperty.bind(hasAllChunksRecordedProperty)
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