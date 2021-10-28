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
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.Artwork
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.FX
import tornadofx.FX.Companion.messages
import tornadofx.get

class BookCard(
    title: String = "",
    slug: String = "",
    coverArt: Artwork? = null,
    newBook: Boolean = false
) : Control() {

    val coverArtProperty = SimpleObjectProperty<Artwork>(coverArt)
    val attributionTextProperty = SimpleStringProperty()
    val titleProperty = SimpleStringProperty(title)
    val slugProperty = SimpleStringProperty()
    val newBookProperty = SimpleBooleanProperty(newBook)

    val addBookTextProperty = SimpleStringProperty(messages["createProject"])
    val onPrimaryActionProperty = SimpleObjectProperty<() -> Unit>()
    val onAddBookActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("book-card")

        coverArtProperty.onChangeAndDoNow { artwork ->
            artwork?.let {
                attributionTextProperty.set(
                    it.attributionText(
                        FX.messages["artworkAttributionTitle"],
                        FX.messages["license"]
                    )
                )
            } ?: attributionTextProperty.set(null)
        }
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
