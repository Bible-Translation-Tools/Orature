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
package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.cards.BookCardSkin
import java.io.File

class BookCard(
    title: String = "",
    projectType: String = "",
    coverArt: File? = null,
    newBook: Boolean = false
) : Control() {

    val coverArtProperty = SimpleObjectProperty<File>(coverArt)
    val titleProperty = SimpleStringProperty(title)
    val projectTypeProperty = SimpleStringProperty(projectType)
    val newBookProperty = SimpleBooleanProperty(newBook)

    val addBookTextProperty = SimpleStringProperty("Add Book")
    val onPrimaryActionProperty = SimpleObjectProperty<() -> Unit>()
    val onAddBookActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("book-card")
    }

    override fun createDefaultSkin(): Skin<*> {
        return BookCardSkin(this)
    }

    fun setOnPrimaryAction(op: () -> Unit) {
        onPrimaryActionProperty.set(op)
    }

    fun setOnAddBookAction(op: () -> Unit) {
        onAddBookActionProperty.set(EventHandler { op.invoke() })
    }
}
