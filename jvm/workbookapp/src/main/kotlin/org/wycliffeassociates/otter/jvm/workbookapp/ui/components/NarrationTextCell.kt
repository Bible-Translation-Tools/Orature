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

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.scene.control.ListCell
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.controls.event.NextVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.PlayVerseEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordAgainEvent
import org.wycliffeassociates.otter.jvm.controls.event.RecordVerseEvent
import org.wycliffeassociates.otter.jvm.controls.narration.NarrationTextItem
import org.wycliffeassociates.otter.jvm.controls.narration.NarrationTextItemState
import org.wycliffeassociates.otter.jvm.controls.narration.narrationTextListview
import tornadofx.FX
import tornadofx.addClass

class NarrationTextItemData(
    val chunk: Chunk,
    val marker: VerseMarker?,
    var hasRecording: Boolean = false,
    var previousChunksRecorded: Boolean = false
) {
    override fun toString(): String {
        return "${chunk.sort}, $hasRecording, $previousChunksRecorded"
    }
}

class NarrationTextCell(
    private val nextChunkText: String,
    private val recordButtonTextProperty: ObservableValue<String>,
    private val isRecordingProperty: ObservableValue<Boolean>,
    private val isRecordingAgainProperty: ObservableValue<Boolean>,
    private val isPlayingProperty: ObservableValue<Boolean>,
    private val recordingIndexProperty: IntegerProperty,
    private val playingVerseProperty: IntegerProperty
) : ListCell<NarrationTextItemData>() {

    private val logger = LoggerFactory.getLogger(NarrationTextCell::class.java)

    private val view = NarrationTextItem()

    init {
        addClass("narration-list__verse-cell")
    }

    override fun updateItem(item: NarrationTextItemData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        val isLast = index == listView.items.lastIndex

        view.isSelectedProperty.set(isSelected)
        view.isLastVerseProperty.set(isLast)

        graphic = view.apply {
            verseLabelProperty.set(item.chunk.title)
            verseTextProperty.set(item.chunk.textItem.text)

            hasRecordingProperty.set(item.hasRecording)
            recordButtonTextProperty.bind(this@NarrationTextCell.recordButtonTextProperty)
            isRecordingProperty.bind(this@NarrationTextCell.isRecordingProperty)
            isRecordingAgainProperty.bind(this@NarrationTextCell.isRecordingAgainProperty)
            isPlayingProperty.bind(this@NarrationTextCell.isPlayingProperty)
            playingVerseIndexProperty.bind(this@NarrationTextCell.playingVerseProperty)
            indexProperty.set(index)
            nextChunkTextProperty.set(nextChunkText)

            onRecordActionProperty.set(EventHandler {
                FX.eventbus.fire(RecordVerseEvent(index, item.chunk))
            })

            onNextVerseActionProperty.set(EventHandler {
                listView.apply {
                    selectionModel.selectNext()

                    // Scroll to the previous verse because scrolling to the active verse will cause
                    // the active verse to move slightly above the viewport. Scrolling -1 will mean that
                    // the active verse won't be on the top and is unpredictably placed, but is still better
                    // than the text needed to actively be narrated being off the screen.
                    scrollTo(selectionModel.selectedIndex - 1)

                    FX.eventbus.fire(NextVerseEvent(selectionModel.selectedIndex, selectionModel.selectedItem.chunk))
                }
            })

            onRecordAgainActionProperty.set(EventHandler {
                FX.eventbus.fire(RecordAgainEvent(index))
            })

            onPlayActionProperty.set(EventHandler {
                item.marker?.let {
                    logger.info("Playing verse index $it")
                    FX.eventbus.fire(PlayVerseEvent(item.marker))
                }
            })

            stateProperty.set(
                computeState(
                    index,
                    isRecording,
                    isRecordingAgain,
                    recordingIndexProperty.value,
                )
            )
        }
    }

    fun computeState(
        index: Int,
        isRecording: Boolean,
        isRecordingAgain: Boolean,
        recordingIndex: Int?
    ): NarrationTextItemState {
        val hasRecording = item.hasRecording
        val previousChunksRecorded = item.previousChunksRecorded
        
        if (!isRecording && !isRecordingAgain && !hasRecording && previousChunksRecorded) {
            return NarrationTextItemState.RECORD
        } else if (isRecording && !isRecordingAgain && index == recordingIndex) {
            return NarrationTextItemState.RECORD_ACTIVE
        } else if (!previousChunksRecorded && !hasRecording || !hasRecording && isRecording && recordingIndex == index - 1) {
            return NarrationTextItemState.RECORD_DISABLED
        } else if (!isRecording && hasRecording) {
            return NarrationTextItemState.RE_RECORD
        } else if (isRecordingAgain && index == recordingIndex) {
            return NarrationTextItemState.RE_RECORD_ACTIVE
        } else {
            return NarrationTextItemState.RE_RECORD_DISABLED
        }
    }
}