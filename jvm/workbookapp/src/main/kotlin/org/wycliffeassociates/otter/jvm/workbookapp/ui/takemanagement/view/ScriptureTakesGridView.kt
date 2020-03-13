package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.controlsfx.control.GridView
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeModel
import tornadofx.*
import kotlin.math.pow

class ScriptureTakesGridView : GridView<Pair<TakeCardType, TakeModel?>>() {

    val gridItems = SimpleObjectProperty<ObservableList<TakeModel>>(FXCollections.observableArrayList())

    init {
        setCellFactory { ScriptureTakesGridCell({}) }
        cellHeightProperty().set(208.0)
        cellWidthProperty().set(392.0)

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
                verticalCellSpacingProperty().value
            ), 1
        )
        val _items = gridItems.get()
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
            println("items ${items.size} columns $columnCount mod $mod")
            val needed = columnCount - mod
            for (i in 1..needed) {
                items.add(Pair(TakeCardType.EMPTY, null))
            }
        }
    }

    private fun calculateColumnCount(width: Double, cellWidth: Double, spacing: Double): Int {
        var count = Math.floor(width / cellWidth).toInt()
        println("Width is: $width cell width: $cellWidth count: $count")
        if (width - (count * cellWidth) - ((count) * spacing) < 0.0) {
            return Math.min(count - 1, 1)
        } else {
            return count
        }
    }
}
