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
import tornadofx.SortedFilteredList
import tornadofx.addClass
import tornadofx.attachTo
import tornadofx.onChange

class NarrationRecordListView<T>(items: ObservableList<T>? = null) : ListView<T>(items) {
    val onOpenAppActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onRecordAgainActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val openInTextProperty = SimpleStringProperty()
    val recordAgainTextProperty = SimpleStringProperty()
    val loadingImageTextProperty = SimpleStringProperty()

    init {
        addClass("wa-list-view")
        orientation = Orientation.HORIZONTAL
    }

    fun setOnOpenApp(op: (verse: T) -> Unit) {
        onOpenAppActionProperty.set(EventHandler {
            op.invoke(it.source as T)
        })
    }

    fun setOnRecordAgain(op: (verse: T) -> Unit) {
        onRecordAgainActionProperty.set(EventHandler {
            op.invoke(it.source as T)
        })
    }
}

fun <T> EventTarget.narrationrecordlistview(values: ObservableList<T>?, op: NarrationRecordListView<T>.() -> Unit = {}) =
    NarrationRecordListView<T>().attachTo(this, op) {
        if (values is SortedFilteredList<T>) values.bindTo(it)
        else it.items = values
    }

fun <T> EventTarget.narrationrecordlistview(
    values: ObservableValue<ObservableList<T>>?,
    op: NarrationRecordListView<T>.() -> Unit = {}
) =
    NarrationRecordListView<T>().attachTo(this, op) {
        fun rebinder() {
            (it.items as? SortedFilteredList<T>)?.bindTo(it)
        }
        it.itemsProperty().bind(values)
        rebinder()
        it.itemsProperty().onChange {
            rebinder()
        }
    }
