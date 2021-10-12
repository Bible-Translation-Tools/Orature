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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.controls.card.BookCardCell
import org.wycliffeassociates.otter.jvm.workbookapp.enums.ProjectType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.BookCardData
import tornadofx.*
import java.util.concurrent.Callable

class BookCell(
    private val projectTypeProperty: SimpleObjectProperty<ProjectType>,
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
            projectTypeProperty.bind(this@BookCell.projectTypeProperty.stringBinding {
                it?.let { FX.messages[it.value] }
            })
            projectPublicDomainProperty.set(item.collection.resourceContainer?.license)

            coverArtProperty.set(item.artwork?.file)
            attributionProperty.set(
                item.artwork?.attributionText(
                    FX.messages["artworkAttributionTitle"],
                    FX.messages["license"]
                )
            )

            setOnMouseClicked {
                onSelected(item)
            }

            disableProperty().bind(
                Bindings.createBooleanBinding(
                    Callable {
                        existingBooks.map {
                            it.target.slug
                        }.contains(item.collection.slug)
                    },
                    existingBooks
                )
            )
        }
    }
}
