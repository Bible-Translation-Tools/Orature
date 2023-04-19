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

import javafx.event.EventHandler
import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.jvm.controls.narration.NarrationTextItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.addClass

class NarrationTextCell(private val nextChunkText: String) : ListCell<ChunkData>() {
    private val view = NarrationTextItem()

    init {
        addClass("narration-list__verse-cell")
    }

    override fun updateItem(item: ChunkData?, empty: Boolean) {
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
            verseTextProperty.set(item.text)

            recordButtonTextProperty.bind(item.recordButtonTextProperty)
            isRecordingProperty.bind(item.isRecordingProperty)
            nextChunkTextProperty.set(nextChunkText)

            onRecordActionProperty.set(EventHandler {
                item.onRecord(item)
            })

            onNextVerseActionProperty.set(EventHandler {
                listView.selectionModel.selectNext()
                listView.scrollTo(item)
                item.onNext(listView.selectionModel.selectedItem)
            })
        }
    }
}
