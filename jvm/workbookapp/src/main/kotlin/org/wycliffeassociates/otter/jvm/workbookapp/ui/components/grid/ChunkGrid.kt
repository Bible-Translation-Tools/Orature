/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.grid

import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.event.ChunkSelectedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkViewData
import tornadofx.*

private const val GRID_COLUMNS = 3

class ChunkGrid(list: List<ChunkViewData>) : GridPane() {

    init {
        hgrow = Priority.ALWAYS

        list.forEachIndexed { index, chunk ->
            val btn = createChunkButton(chunk)
            btn.prefWidthProperty().bind(this.widthProperty().divide(GRID_COLUMNS.toDouble()))
            this.add(btn, index % GRID_COLUMNS, index / GRID_COLUMNS)
        }
    }

    private fun createChunkButton(chunk: ChunkViewData): Button {
        return Button(chunk.number.toString()).apply {
            addClass("btn", "btn--secondary", "btn--borderless", "chunk-item")

            graphicProperty().bind(
                chunk.selectedChunkProperty.objectBinding {
                    val selected = it == chunk.number
                    isFocusTraversable = !selected
                    isMouseTransparent = selected
                    this.togglePseudoClass("selected", selected)
                    this.togglePseudoClass("completed", chunk.isCompleted)
                    when {
                        chunk.isCompleted -> FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply {
                            addClass("chunk-item__icon")
                        }
                        selected -> FontIcon(MaterialDesign.MDI_BOOKMARK).apply {
                            addClass("chunk-item__icon")
                        }
                        else -> FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE).apply {
                            addClass("chunk-item__icon")
                        }
                    }
                }
            )

            action {
                FX.eventbus.fire(ChunkSelectedEvent(chunk.number))
            }
        }
    }
}

fun EventTarget.chunkGrid(
    list: List<ChunkViewData>,
    op: ChunkGrid.() -> Unit = {}
) = ChunkGrid(list).attachTo(this, op)