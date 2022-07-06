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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.binding.Bindings
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
import javafx.scene.input.KeyCode
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.controls.card.BookCardCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.BookCardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.book.matchedExistingBook
import tornadofx.*

class BookCell(
    private val existingBooks: ObservableList<Workbook> = observableListOf(),
    private val onSelected: (BookCardData) -> Unit
) : ListCell<BookCardData>() {

    private val view = BookCardCell()

    override fun updateItem(item: BookCardData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            bookSlugProperty.set(item.collection.slug.uppercase())
            bookNameProperty.set(item.collection.titleKey)
            licenseProperty.set(item.collection.resourceContainer?.license)
            coverArtProperty.bind(item.artworkProperty)
            attributionProperty.bind(item.attributionProperty)

            setOnAction {
                onSelected(item)
            }

            if (isSelected and !isDisabled) {
                listView.setOnKeyPressed {
                    when (it.code) {
                        KeyCode.ENTER, KeyCode.SPACE -> onSelected(item)
                    }
                }
            }
        }

        disableProperty().bind(
            Bindings.createBooleanBinding(
                {
                    existingBooks.any {
                        matchedExistingBook(item, it)
                    }
                },
                existingBooks
            )
        )
        mouseTransparentProperty().bind(disableProperty())
    }
}
