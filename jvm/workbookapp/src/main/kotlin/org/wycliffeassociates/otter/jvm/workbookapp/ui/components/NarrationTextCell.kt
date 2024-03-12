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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.IntegerProperty
import javafx.beans.value.ObservableValue
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.ListCell
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.narration.statemachine.NarrationStateType
import org.wycliffeassociates.otter.jvm.controls.event.*
import org.wycliffeassociates.otter.jvm.controls.narration.NarrationTextItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.NarratableItemData
import tornadofx.*

class NarrationTextCell(
    private val nextChunkText: String,
    private val recordButtonTextProperty: ObservableValue<String>,
    private val narrationStateProperty: ObservableValue<NarrationStateType>,
    highlightedVerseProperty: IntegerProperty,
) : ListCell<NarratableItemData>() {

    private val logger = LoggerFactory.getLogger(NarrationTextCell::class.java)

    private val view = NarrationTextItem()

    private val shouldHighlight = booleanBinding(highlightedVerseProperty, indexProperty()) {
        highlightedVerseProperty.value == index
    }

    init {
        addClass("narration-list__verse-cell")
    }

    override fun updateItem(item: NarratableItemData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        val isLast = index == listView.items.lastIndex

        view.isSelectedProperty.set(isSelected)
        view.isLastVerseProperty.set(isLast)

        graphic = view.apply {

            val title = if (item.chunk.label == "verse") item.chunk.title else ""

            verseLabelProperty.set(title)

            verseTextProperty.set(item.chunk.textItem.text)

            recordButtonTextProperty.bind(this@NarrationTextCell.recordButtonTextProperty)
            isHighlightedProperty.bind(shouldHighlight)

            narrationStateProperty.bind(this@NarrationTextCell.narrationStateProperty)

            indexProperty.set(index)
            nextChunkTextProperty.set(nextChunkText)

            onRecordActionProperty.set(DebouncedEventHandler {
                FX.eventbus.fire(RecordVerseEvent(index, item.chunk))
            })

            onNextVerseActionProperty.set(DebouncedEventHandler {
                listView.apply {

                    try {
                        selectionModel.selectIndices(index)
                        selectionModel.selectNext()

                        // Scroll to the previous verse because scrolling to the active verse will cause
                        // the active verse to move slightly above the viewport. Scrolling -1 will mean that
                        // the active verse won't be on the top and is unpredictably placed, but is still better
                        // than the text needed to actively be narrated being off the screen.
                        scrollTo(selectionModel.selectedIndex - 1)
                    } catch (e: Exception) {
                        logger.error("Error in selecting and scrolling to a Teleprompter item", e)
                    }

                    val nextVerseIndex = index + 1
                    val nextVerse = items.getOrNull(nextVerseIndex)
                    nextVerse?.let {
                        FX.eventbus.fire(NextVerseEvent(nextVerseIndex, nextVerse.chunk))
                    }
                        ?: logger.error("Tried to select an invalid next verse! Tried to select: $nextVerseIndex from index $index")
                }
            })

            onRecordAgainActionProperty.set(DebouncedEventHandler {
                FX.eventbus.fire(RecordAgainEvent(index))
            })

            onPlayActionProperty.set(DebouncedEventHandler {
                item.marker?.let {
                    FX.eventbus.fire(PlayVerseEvent(it))
                }
            })

            onPauseActionProperty.set(DebouncedEventHandler {
                item.marker?.let {
                    FX.eventbus.fire(PauseEvent())
                }
            })

            onSaveRecordingActionProperty.set(DebouncedEventHandler {
                FX.eventbus.fire(SaveRecordingEvent(index))
            })

            onBeginRecordingAction.set(DebouncedEventHandler {
                FX.eventbus.fire(BeginRecordingEvent(index, item.chunk))
            })

            onPauseRecordingAction.set(DebouncedEventHandler {
                FX.eventbus.fire(PauseRecordingEvent(index, item.chunk))
            })

            onPauseRecordAgainAction.set(DebouncedEventHandler {
                FX.eventbus.fire(PauseRecordAgainEvent(index, item.chunk))
            })

            onResumeRecordingAction.set(DebouncedEventHandler {
                FX.eventbus.fire(ResumeRecordingEvent(index, item.chunk))
            })

            onResumeRecordingAgainAction.set(DebouncedEventHandler {
                FX.eventbus.fire(ResumeRecordingAgainEvent(index, item.chunk))
            })

            verseStateProperty.set(item.verseState)

            isPlayEnabledProperty.set(item.isPlayOptionEnabled)
        }
    }

    private inline fun <T : Event> DebouncedEventHandler(crossinline op: () -> Unit): EventHandler<T> {
        return EventHandler {
            if (System.currentTimeMillis() - timeOfLastAction > 500L) {
                timeOfLastAction = System.currentTimeMillis()
                op.invoke()
            }
        }
    }

    companion object {
        var timeOfLastAction = System.currentTimeMillis()
    }
}