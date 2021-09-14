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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.jvm.controls.card.EmptyCardCell
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import tornadofx.*
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

class ScriptureTakeCell : ListCell<TakeCardModel>() {

    private var takeCard = ScriptureTakeCard()

    override fun updateItem(item: TakeCardModel?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = EmptyCardCell().apply {
                addClass("card--scripture-take--empty")
            }
            return
        }

        graphic = takeCard.apply {
            takeProperty().set(item.take)
            audioPlayerProperty().set(item.audioPlayer)
            selectedProperty().set(item.selected)
            lastModifiedProperty().set(
                SimpleDateFormat.getDateTimeInstance(
                    SimpleDateFormat.SHORT,
                    SimpleDateFormat.SHORT,
                    Locale.getDefault()
                ).format(item.take.file.lastModified())
            )
            takeLabelProperty().set(
                MessageFormat.format(
                    FX.messages["takeTitle"],
                    FX.messages["take"],
                    item.take.number
                )
            )
        }
    }
}
