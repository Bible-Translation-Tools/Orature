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

import javafx.collections.FXCollections
import dev.jbs.gridview.control.GridView
import impl.dev.jbs.gridview.skin.GridViewSkin
import javafx.beans.binding.BooleanBinding
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import tornadofx.*
import kotlin.math.pow

class ScriptureTakesGridView(
    val contentIsMarkable: BooleanBinding,
    val recordNewTake: () -> Unit
) : GridView<Pair<TakeCardType, TakeCardModel?>>() {

    val gridItems = FXCollections.observableArrayList<TakeCardModel>()

    init {
        setCellFactory { ScriptureTakesGridCell(recordNewTake, contentIsMarkable) }
        cellHeightProperty().set(148.0)
        cellWidthProperty().set(332.0)

        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        fitToParentWidth()
        fitToParentHeight()

        widthProperty().onChange {
            updateItems()
        }

        gridItems.onChangeAndDoNow {
            updateItems()
        }
    }

    private fun updateItems() {
        val columnCount = Math.max(
            calculateColumnCount(
                widthProperty().value,
                cellWidthProperty().value,
                horizontalCellSpacingProperty().value
            ), 1
        )
        val _items = gridItems
        if (_items == null || _items.isEmpty()) {
            items.clear()
            items.add(Pair(TakeCardType.NEW, null))
            // start from 2 because we just added the new recording card
            for (i in 2..columnCount.toDouble().pow(2.0).toInt()) {
                items.add(Pair(TakeCardType.EMPTY, null))
            }
        } else {
            items.clear()
            items.add(Pair(TakeCardType.NEW, null))
            items.addAll(_items.map { Pair(TakeCardType.TAKE, it) })
            val mod = items.size % columnCount
            val needed = columnCount - mod
            for (i in 1..needed) {
                items.add(Pair(TakeCardType.EMPTY, null))
            }
            val remaining = columnCount.toDouble().pow(2.0).toInt() - items.size
            if (remaining > 0) {
                for (i in 1..remaining) {
                    items.add(Pair(TakeCardType.EMPTY, null))
                }
            }
        }
    }

    private fun calculateColumnCount(width: Double, cellWidth: Double, spacing: Double): Int {
        /* Skin can be null while the layout is being created and inflated
        so give a rough guess at the count in case but prefer what the skin is using
        in small situations where the difference is off, the skin will display more or less items
        in a row than expected and the result would mean adding the wrong number of blank cards to completely
        fill out the row */
        if (skin != null && skin is GridViewSkin<*>) {
            return (skin as GridViewSkin<*>).computeMaxCellsInRow()
        } else {
            var count = Math.floor(width / cellWidth).toInt()
            if (width - (count * cellWidth) - ((count - 1) * spacing * 2) < 0.0) {
                return Math.max(count - 1, 1)
            } else {
                return count
            }
        }
    }
}
