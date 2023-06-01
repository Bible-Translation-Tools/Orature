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
import tornadofx.FX
import tornadofx.FXEvent
import tornadofx.addClass

class NarrationRecordCell(
    private val openInText: String,
    private val recordAgainText: String,
    private val loadingImageText: String,
    private val goToVerseText: String
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

            isPlayingProperty.bind(item.isPlayingProperty)
            isRecordingProperty.bind(item.isRecordingProperty)
            playbackPositionProperty.bind(item.playbackPositionProperty)
            totalFramesProperty.bind(item.totalFramesProperty)

            waveformProperty.bind(item.imageProperty)
            invertedWaveformProperty.bind(item.invertedImageProperty)
            waveformLoadingProperty.bind(item.imageLoadingProperty)

            waveformDrawableProperty.bind(item.waveformProperty)
            volumebarDrawableProperty.bind(item.volumeBarProperty)

            loadingImageTextProperty.set(loadingImageText)
            openInTextProperty.set(openInText)
            recordAgainTextProperty.set(recordAgainText)
            goToVerseTextProperty.set(goToVerseText)

            onPlayActionProperty.set(EventHandler {
                //FX.eventbus.fire(PlayVerseEvent(item))
            })
            onOpenAppActionProperty.set(EventHandler {
                //FX.eventbus.fire(OpenInAppEvent(item))
            })
            onRecordAgainActionProperty.set(EventHandler {
                //FX.eventbus.fire(RecordAgainEvent(item))
            })
            onWaveformClickActionProperty.set(EventHandler {
                //FX.eventbus.fire(WaveformClickedEvent(index))
            })
        }
    }
}

//class PlayVerseEvent(val data: ChunkData) : FXEvent()
//class OpenInAppEvent(val data: ChunkData) : FXEvent()
//class RecordAgainEvent(val data: ChunkData) : FXEvent()
//class WaveformClickedEvent(val index: Int) : FXEvent()
