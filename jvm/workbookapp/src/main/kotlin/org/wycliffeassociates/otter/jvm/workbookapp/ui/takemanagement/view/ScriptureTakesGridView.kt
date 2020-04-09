package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import javafx.collections.FXCollections
import dev.jbs.gridview.control.GridView
import impl.dev.jbs.gridview.skin.GridViewSkin
import javafx.scene.layout.Priority
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

    fun closePlayers() {
        gridItems.forEach {
            it.audioPlayer.pause()
            it.audioPlayer.close()
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
