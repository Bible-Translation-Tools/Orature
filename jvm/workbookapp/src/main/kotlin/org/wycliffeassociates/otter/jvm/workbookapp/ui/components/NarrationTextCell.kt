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

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.jvm.controls.narration.NarrationTextItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.addClass

class NarrationTextCell : ListCell<ChunkData>() {
    private val view = NarrationTextItem()

    val beginRecordingTextCellProperty = SimpleStringProperty()
    val pauseRecordingTextCellProperty = SimpleStringProperty()
    val resumeRecordingTextCellProperty = SimpleStringProperty()
    val nextChunkTextCellProperty = SimpleStringProperty()

    val onRecordActionCellProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("narration-list__verse-cell")
    }

    override fun updateItem(item: ChunkData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        view.isActiveProperty.set(isSelected)
        view.isLastVerseProperty.set(index == listView.items.lastIndex)

        graphic = view.apply {
            verseLabelProperty.set(item.title)
            verseTextProperty.set(item.text)

            beginRecordingTextProperty.set(beginRecordingTextCellProperty.value)
            pauseRecordingTextProperty.set(pauseRecordingTextCellProperty.value)
            resumeRecordingTextProperty.set(resumeRecordingTextCellProperty.value)
            nextChunkTextProperty.set(nextChunkTextCellProperty.value)

            onRecordActionProperty.set(EventHandler {
                onRecordActionCellProperty.value?.handle(ActionEvent(item, null))
            })

            setOnNextVerse {
                listView.selectionModel.selectNext()
                listView.scrollTo(item)
            }
        }
    }

    fun setOnRecord(op: () -> Unit) {
        onRecordActionCellProperty.set(EventHandler {
            op.invoke()
        })
    }
}
