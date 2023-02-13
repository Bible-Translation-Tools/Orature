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
package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.ListView
import javafx.scene.control.ScrollBar
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.jvm.utils.findChildren
import org.wycliffeassociates.otter.jvm.utils.virtualFlow
import tornadofx.*

class NarrationListView(items: ObservableList<Chunk>? = null) : ListView<Chunk>(items) {
    val selectedVerseLabelProperty = SimpleStringProperty()
    val onSelectedVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val beginRecordingTextProperty = SimpleStringProperty()
    val pauseRecordingTextProperty = SimpleStringProperty()
    val resumeRecordingTextProperty = SimpleStringProperty()
    val nextChunkTextProperty = SimpleStringProperty()

    private val onRecordActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("wa-list-view")

        setCellFactory {
            NarrationTextCell().apply {
                beginRecordingTextCellProperty.bind(beginRecordingTextProperty)
                pauseRecordingTextCellProperty.bind(pauseRecordingTextProperty)
                resumeRecordingTextCellProperty.bind(resumeRecordingTextProperty)
                nextChunkTextCellProperty.bind(nextChunkTextProperty)

                setOnRecord {
                    onRecordActionProperty.value?.handle(ActionEvent(item, null))
                }
            }
        }

        itemsProperty().onChange {
            selectionModel.select(0)
        }

        skinProperty().onChange {
            it?.let {
                try {
                    val scrollBar = virtualFlow().findChildren<ScrollBar>(true).singleOrNull { node ->
                        node.orientation == Orientation.VERTICAL
                    }
                    scrollBar?.valueProperty()?.onChange {
                        val current = selectionModel.selectedIndex
                        val first = virtualFlow().firstVisibleCell.index
                        val last = virtualFlow().lastVisibleCell.index

                        if (current !in (first..last)) {
                            selectedVerseLabelProperty.set(selectedItem?.label)
                            onSelectedVerseActionProperty.set(EventHandler {
                                selectionModel.select(current)
                                scrollTo(current)
                            })
                        } else {
                            selectedVerseLabelProperty.set(null)
                            onSelectedVerseActionProperty.set(null)
                        }
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setOnRecord(op: (verse: Chunk) -> Unit) {
        onRecordActionProperty.set(EventHandler {
            op.invoke(it.source as Chunk)
        })
    }
}

fun EventTarget.narrationtextlistview(values: ObservableList<Chunk>?, op: NarrationListView.() -> Unit = {}) =
    NarrationListView().attachTo(this, op) {
        if (values is SortedFilteredList<Chunk>) values.bindTo(it)
        else it.items = values
}

fun EventTarget.narrationtextlistview(
    values: ObservableValue<ObservableList<Chunk>>?,
    op: NarrationListView.() -> Unit = {}
) =
    NarrationListView().attachTo(this, op) {
    fun rebinder() {
        (it.items as? SortedFilteredList<Chunk>)?.bindTo(it)
    }
    it.itemsProperty().bind(values)
    rebinder()
    it.itemsProperty().onChange {
        rebinder()
    }
}