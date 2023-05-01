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

import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.controls.narration.NarrationTextItem
import tornadofx.FX
import tornadofx.FXEvent
import tornadofx.addClass

class NarrationTextCell(
    private val nextChunkText: String,
    private val recordButtonTextProperty: ObservableValue<String>,
    private val isRecordingProperty: ObservableValue<Boolean>
) : ListCell<Chunk>() {
    private val view = NarrationTextItem()

    init {
        addClass("narration-list__verse-cell")
    }

    override fun updateItem(item: Chunk?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        val isLast = index == listView.items.lastIndex

        view.isSelectedProperty.set(isSelected)
        view.isLastVerseProperty.set(isLast)

        graphic = view.apply {
            verseLabelProperty.set(item.title)
            verseTextProperty.set(item.textItem.text)

            recordButtonTextProperty.bind(this@NarrationTextCell.recordButtonTextProperty)
            isRecordingProperty.bind(this@NarrationTextCell.isRecordingProperty)
            nextChunkTextProperty.set(nextChunkText)

            onRecordActionProperty.set(EventHandler {
                FX.eventbus.fire(RecordVerseEvent(item))
            })

            onNextVerseActionProperty.set(EventHandler  {
                listView.apply {
                    selectionModel.selectNext()
                    scrollTo(selectionModel.selectedItem)

                    FX.eventbus.fire(NextVerseEvent(selectionModel.selectedItem))
                }
            })
        }
    }
}

class NextVerseEvent(val data: Chunk) : FXEvent()
class RecordVerseEvent(val data: Chunk) : FXEvent()