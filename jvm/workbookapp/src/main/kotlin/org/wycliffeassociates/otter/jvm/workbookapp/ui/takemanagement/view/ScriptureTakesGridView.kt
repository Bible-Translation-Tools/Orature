package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import dev.jbs.gridview.control.GridView
import impl.dev.jbs.gridview.skin.GridViewSkin
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeModel
import tornadofx.*
import kotlin.math.pow

class ScriptureTakesGridView(
    val recordNewTake: () -> Unit
) : GridView<Pair<TakeCardType, TakeModel?>>() {

    val gridItems = FXCollections.observableArrayList<TakeModel>()

    init {
        setCellFactory { ScriptureTakesGridCell(recordNewTake) }
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
                verticalCellSpacingProperty().value
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
            println("items ${items.size} columns $columnCount mod $mod")
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
        var count = Math.floor(width / cellWidth).toInt()
        println("Width is: $width cell width: $cellWidth count: $count")
        if (width - (count * cellWidth) - ((count) * 2.0 * spacing) < 0.0) {
            return Math.min(count - 1, 1)
        } else {
            return count
        }
    }
}
