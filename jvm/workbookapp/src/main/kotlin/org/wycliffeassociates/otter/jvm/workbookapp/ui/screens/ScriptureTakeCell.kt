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

import javafx.animation.TranslateTransition
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.util.Duration
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.controls.card.EmptyCardCell
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import tornadofx.*
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

class ScriptureTakeCell(
    private val onDelete: (Take) -> Unit,
    private val onEdit: (Take) -> Unit,
    private val onSelected: (Take) -> Unit
) : ListCell<TakeCardModel>() {

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
            setOnTakeDelete { onDelete(item.take) }
            setOnTakeEdit { onEdit(item.take) }
            setOnTakeSelected {
                if (isAnimatingProperty.value || item.selected) {
                    return@setOnTakeSelected
                }

                isAnimatingProperty.set(true)
                animate(item) {
                    onSelected(item.take)
                }
            }
        }
    }

    private fun animate(takeModel: TakeCardModel, callback: () -> Unit) {
        shiftOtherNodes(takeModel)

        val parentY = takeCard.parent.layoutY
        takeCard.styleClass.add("selected")

        // move selected node to top of the list
        val ttUp = TranslateTransition(Duration.millis(600.0), takeCard)
        ttUp.toY = -parentY
        ttUp.onFinished = EventHandler {
            takeCard.styleClass.remove("selected")
            revertAnimation(takeCard) { takeCard.isAnimatingProperty.set(false) }
            callback()
        }
        ttUp.play()
    }

    private fun shiftOtherNodes(takeModel: TakeCardModel) {
        val selectedIndex = listView.items.indexOf(takeModel)
        for (item in listView.items) {
            if (listView.items.indexOf(item) < selectedIndex) {
                moveDown(takeCard)
            }
        }
    }

    private fun moveDown(node: Node) {
        val distance = node.boundsInLocal.height + 5
        val tt = TranslateTransition(Duration.millis(600.0), node)
        tt.byY = distance
        tt.onFinished = EventHandler {
            revertAnimation(node)
        }
        tt.play()
    }

    private fun revertAnimation(node: Node, onFinish: () -> Unit = { }) {
        val distance = node.translateY
        val ttRevertY = TranslateTransition(Duration.millis(1.0), node)
        ttRevertY.byY = -distance
        ttRevertY.onFinished = EventHandler {
            onFinish()
        }
        ttRevertY.play()
    }
}
