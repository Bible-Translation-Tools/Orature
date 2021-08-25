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

import dev.jbs.gridview.control.GridCell
import javafx.beans.binding.BooleanBinding
import org.wycliffeassociates.otter.jvm.controls.card.EmptyCardCell
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.card.NewRecordingCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import tornadofx.*

class ScriptureTakesGridCell(
    newRecordingAction: () -> Unit,
    private val contentIsMarkable: BooleanBinding
) : GridCell<Pair<TakeCardType, TakeCardModel?>>() {

    private var rect = EmptyCardCell()
    private var takeCard = ScriptureTakeCard()
    private var newRecording = NewRecordingCard(
        FX.messages["newTake"],
        FX.messages["record"],
        newRecordingAction
    )

    override fun updateItem(item: Pair<TakeCardType, TakeCardModel?>?, empty: Boolean) {
        super.updateItem(item, empty)

        if (!empty && item != null) {
            if (item.first == TakeCardType.NEW) {
                graphic = newRecording
            } else if (
                item.first == TakeCardType.TAKE &&
                item.second != null && !item.second!!.selected
            ) {
                val model = item.second!!
                takeCard.takeProperty().set(model.take)
                takeCard.editTextProperty().set(model.editText)
                takeCard.audioPlayerProperty().set(model.audioPlayer)
                takeCard.deleteTextProperty().set(model.deleteText)
                takeCard.markerTextProperty().set(model.markerText)
                takeCard.playTextProperty().set(model.playText)
                takeCard.pauseTextProperty().set(model.pauseText)
                takeCard.timestampProperty().set(model.take.createdTimestamp.toString())
                takeCard.takeNumberProperty().set(model.take.number.toString())
                takeCard.allowMarkerProperty().set(contentIsMarkable.value)

                takeCard.prefWidthProperty().bind(widthProperty())
                takeCard.prefHeightProperty().bind(heightProperty())
                this.graphic = takeCard
            } else {
                rect.apply {
                    addClass("card--scripture-take--empty")
                    prefHeightProperty().bind(this@ScriptureTakesGridCell.heightProperty())
                    prefWidthProperty().bind(this@ScriptureTakesGridCell.widthProperty())
                }
                this.graphic = rect
            }
        }
    }
}
