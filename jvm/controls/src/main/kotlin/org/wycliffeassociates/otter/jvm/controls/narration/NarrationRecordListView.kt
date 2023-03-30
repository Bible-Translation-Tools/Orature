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

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.ListView
import tornadofx.*

class NarrationRecordListView<T>(items: ObservableList<T>? = null) : ListView<T>(items) {
    val isRecordingProperty = SimpleBooleanProperty()
    val isRecordingPausedProperty = SimpleBooleanProperty()

    private val isRecordingActiveProperty = isRecordingProperty.and(isRecordingPausedProperty.not())

    init {
        addClass("wa-list-view", "narration-record__list-view")
        orientation = Orientation.HORIZONTAL

        isRecordingActiveProperty.onChange {
            toggleClass("recording", it)
        }

        disableProperty().bind(isRecordingActiveProperty)
        styleProperty().bind(isRecordingActiveProperty.stringBinding {
            if (it == true) "-fx-opacity: 1" else ""
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
