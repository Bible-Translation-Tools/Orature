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
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.ListView
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import tornadofx.SortedFilteredList
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.onChange

class NarrationRecordListView(items: ObservableList<Chunk>? = null) : ListView<Chunk>(items) {
    private val onPlayActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onOpenAppActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onRecordAgainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("wa-list-view")

        setCellFactory {
            NarrationRecordCell().apply {
                setOnPlay {
                    onPlayActionProperty.value?.handle(ActionEvent(item, null))
                }

                setOnOpenApp {
                    onOpenAppActionProperty.value?.handle(ActionEvent(item, null))
                }

                setOnRecordAgain {
                    onRecordAgainActionProperty.value?.handle(ActionEvent(item, null))
                }
            }
        }
        orientation = Orientation.HORIZONTAL
    }

    fun setOnPlay(op: (verse: Chunk) -> Unit) {
        onPlayActionProperty.set(EventHandler {
            op.invoke(it.source as Chunk)
        })
    }

    fun setOnOpenApp(op: (verse: Chunk) -> Unit) {
        onOpenAppActionProperty.set(EventHandler {
            op.invoke(it.source as Chunk)
        })
    }

    fun setOnRecordAgain(op: (verse: Chunk) -> Unit) {
        onRecordAgainActionProperty.set(EventHandler {
            op.invoke(it.source as Chunk)
        })
    }
}

fun EventTarget.narrationrecordlistview(values: ObservableList<Chunk>?, op: NarrationRecordListView.() -> Unit = {}) =
    NarrationRecordListView().attachTo(this, op) {
        if (values is SortedFilteredList<Chunk>) values.bindTo(it)
        else it.items = values
    }

fun EventTarget.narrationrecordlistview(
    values: ObservableValue<ObservableList<Chunk>>?,
    op: NarrationRecordListView.() -> Unit = {}
) =
    NarrationRecordListView().attachTo(this, op) {
        fun rebinder() {
            (it.items as? SortedFilteredList<Chunk>)?.bindTo(it)
        }
        it.itemsProperty().bind(values)
        rebinder()
        it.itemsProperty().onChange {
            rebinder()
        }
    }