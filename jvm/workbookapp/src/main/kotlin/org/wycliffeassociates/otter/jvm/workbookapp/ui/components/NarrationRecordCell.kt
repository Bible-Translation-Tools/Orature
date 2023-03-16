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
import org.wycliffeassociates.otter.jvm.controls.narration.NarrationRecordItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.addClass

class NarrationRecordCell : ListCell<ChunkData>() {
    private val view = NarrationRecordItem()

    val openInTextCellProperty = SimpleStringProperty()
    val recordAgainTextCellProperty = SimpleStringProperty()
    val loadingImageTextCellProperty = SimpleStringProperty()

    val onOpenAppActionCellProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordAgainActionCellProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("narration-record__verse-cell")
    }

    override fun updateItem(item: ChunkData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            verseLabelProperty.set(item.title)
            audioPlayerProperty.set(item.player)

            waveformProperty.bind(item.imageProperty)
            waveformLoadingProperty.bind(item.imageLoadingProperty)

            loadingImageTextProperty.set(loadingImageTextCellProperty.value)
            openInTextProperty.set(openInTextCellProperty.value)
            recordAgainTextProperty.set(recordAgainTextCellProperty.value)

            onOpenAppActionProperty.set(EventHandler {
                onOpenAppActionCellProperty.value?.handle(ActionEvent(item, null))
            })
            onRecordAgainActionProperty.set(EventHandler {
                onRecordAgainActionCellProperty.value?.handle(ActionEvent(item, null))
            })
        }
    }
}
