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

import javafx.scene.control.ListCell
import javafx.scene.input.KeyCode
import org.wycliffeassociates.otter.common.utils.capitalizeString
import org.wycliffeassociates.otter.jvm.controls.utils.simulateKeyPress
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import tornadofx.*
import java.text.MessageFormat

class ChunkCell(
    private val orientationScale: Double
) : ListCell<CardData>() {
    private val view = ChunkItem()

    override fun updateItem(item: CardData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            showTakesProperty.set(false)
            orientationScaleProperty.set(orientationScale)

            chunkTitleProperty.set(
                MessageFormat.format(
                    FX.messages["chunkTitle"],
                    FX.messages[item.item].capitalizeString(),
                    item.bodyText
                )
            )

            setOnChunkOpen { item.onChunkOpen(item) }
            setOnTakeSelected {
                item.onTakeSelected(item, it)
                refreshTakes()
            }

            hasSelectedProperty.set(item.takes.size > 0)

            refreshTakes()

            setOnKeyReleased {
                when (it.code) {
                    KeyCode.ENTER, KeyCode.SPACE -> {
                        toggleShowTakes()
                    }
                    KeyCode.DOWN -> simulateKeyPress(KeyCode.TAB)
                    KeyCode.UP -> simulateKeyPress(KeyCode.TAB, shiftDown = true)
                }
            }

            setOnMouseClicked {
                requestFocus()
                toggleShowTakes()
            }
        }
    }

    private fun refreshTakes() {
        val sorted = item.takes.sortedWith(
            compareByDescending<TakeModel> { it.selected }
                .thenByDescending { it.take.file.lastModified() }
        )
        view.takes.setAll(sorted)
    }
}
