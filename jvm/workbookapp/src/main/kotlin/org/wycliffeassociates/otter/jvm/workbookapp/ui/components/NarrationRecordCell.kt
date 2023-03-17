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
import org.wycliffeassociates.otter.jvm.controls.narration.NarrationRecordItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChunkData
import tornadofx.addClass

class NarrationRecordCell(
    private val openInText: String,
    private val recordAgainText: String,
    private val loadingImageText: String,
    private val goToVerseText: String,
    private val onOpenApp: (ChunkData) -> Unit,
    private val onRecordAgain: (ChunkData) -> Unit,
    private val onWaveformClicked: (ChunkData) -> Unit
) : ListCell<ChunkData>() {
    private val view = NarrationRecordItem()

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
            invertedWaveformProperty.bind(item.invertedImageProperty)
            waveformLoadingProperty.bind(item.imageLoadingProperty)

            loadingImageTextProperty.set(loadingImageText)
            openInTextProperty.set(openInText)
            recordAgainTextProperty.set(recordAgainText)
            goToVerseTextProperty.set(goToVerseText)

            onOpenAppActionProperty.set(EventHandler {
                onOpenApp(item)
            })
            onRecordAgainActionProperty.set(EventHandler {
                onRecordAgain(item)
            })
            onWaveformClickActionProperty.set(EventHandler {
                onWaveformClicked(item)
            })
        }
    }
}
