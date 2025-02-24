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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.button.debouncedButton
import org.wycliffeassociates.otter.jvm.controls.event.AddMarkerEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class AddMarkerSplitButton : HBox() {

    val canAddVerseMarkerProperty = SimpleBooleanProperty()
    val canAddBookMarkerProperty = SimpleBooleanProperty()
    val canAddChapterMarkerProperty = SimpleBooleanProperty()

    private lateinit var dropDownButton: Button

    private val menu = AddMarkerMenu().also { menu ->
        menu.setOnShowing { dropDownButton.addPseudoClass("active") }
        menu.setOnHidden { dropDownButton.removePseudoClass("active") }

        menu.canAddBookMarkerProperty.bind(canAddBookMarkerProperty)
        menu.canAddChapterMarkerProperty.bind(canAddChapterMarkerProperty)
    }

    init {
        addClass("chapter-selector", "chapter-selector__controls")
        debouncedButton(messages["addVerseMarker"]) {
            addClass("btn", "btn--primary", "chapter-selector__btn-prev")
            graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_PLUS)
            fitToParentHeight()

            enableWhen(canAddVerseMarkerProperty)
            action {
                FX.eventbus.fire(AddMarkerEvent(VerseMarker::class))
            }
        }
        button {
            dropDownButton = this
            addClass("btn", "btn--primary", "chapter-selector__btn-next")
            graphic = FontIcon(MaterialDesign.MDI_CHEVRON_DOWN)

            disableWhen {
                canAddBookMarkerProperty.not().and(canAddChapterMarkerProperty.not())
            }

            action {
                val screenBound = localToScreen(boundsInLocal)
                menu.show(FX.primaryStage)
                menu.x = screenBound.centerX - menu.width + this.width
                menu.y = screenBound.centerY - menu.height
            }
        }
    }
}

class AddMarkerMenu : ContextMenu() {

    val canAddBookMarkerProperty = SimpleBooleanProperty()
    val canAddChapterMarkerProperty = SimpleBooleanProperty()

    init {
        addClass("wa-context-menu")

        val addBookOption = MenuItem().apply {
            addClass("btn", "btn--tertiary", "btn--borderless")
            graphic = label(messages["addBookMarker"]) {
                graphic = FontIcon(MaterialDesign.MDI_BOOK)
                tooltip(text)
            }

            enableWhen(canAddBookMarkerProperty)

            action {
                FX.eventbus.fire(AddMarkerEvent(BookMarker::class))
            }
        }

        val addChapterOption = MenuItem().apply {
            addClass("btn", "btn--tertiary", "btn--borderless")
            graphic = label(messages["addChapterMarker"]) {
                graphic = FontIcon(MaterialDesign.MDI_FILE_DOCUMENT)
                tooltip(text)
            }

            enableWhen(canAddChapterMarkerProperty)

            action {
                FX.eventbus.fire(AddMarkerEvent(ChapterMarker::class))
            }

        }

        items.setAll(addBookOption, addChapterOption)
    }

}

fun EventTarget.addMarkerSplitButton(op: AddMarkerSplitButton.() -> Unit = {}) = AddMarkerSplitButton().attachTo(this, op)