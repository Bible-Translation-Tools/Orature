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

import javafx.beans.property.Property
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.input.KeyCode
import org.wycliffeassociates.otter.common.utils.capitalizeString
import org.wycliffeassociates.otter.jvm.utils.findChild
import org.wycliffeassociates.otter.jvm.utils.simulateKeyPress
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import tornadofx.*
import java.text.MessageFormat

class ChunkCell(
    private val orientationScale: Double,
    private val focusedChunkProperty: Property<ChunkItem>
) : ListCell<CardData>() {
    private val view = ChunkItem()
    init {
        addClass("chunk-list-cell")
    }

    override fun updateItem(item: CardData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        // mouseReleased avoids drag click side effect
        setOnMouseReleased {
            view.requestFocus()
            view.toggleShowTakes()
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
                findChild<Button>()?.requestFocus()
            }

            hasSelectedProperty.set(item.takes.size > 0)

            refreshTakes()

            focusedProperty().onChange {
                if (isFocused) {
                    focusedChunkProperty.value?.hideTakes()
                    focusedChunkProperty.value = this
                }
            }

            setOnKeyReleased {
                when (it.code) {
                    KeyCode.ENTER, KeyCode.SPACE -> {
                        toggleShowTakes()
                    }
                    KeyCode.DOWN, KeyCode.UP -> {
                        val isDown = it.code == KeyCode.DOWN
                        if (isDown && listView.selectionModel.selectedIndex == 0) {
                            return@setOnKeyReleased
                        }

                        if (it.target is ChunkItem) {
                            hideTakes()
                            simulateKeyPress(
                                KeyCode.TAB,
                                shiftDown = it.code == KeyCode.UP
                            )
                        }
                    }
                    KeyCode.ESCAPE -> hideTakes()
                }
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
