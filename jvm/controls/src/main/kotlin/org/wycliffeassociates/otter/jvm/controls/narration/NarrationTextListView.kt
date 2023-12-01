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

import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.ListView
import javafx.scene.control.ScrollBar
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.findChildren
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.utils.virtualFlow
import tornadofx.*

class NarrationTextListView<T>(items: ObservableList<T>? = null) : ListView<T>(items) {
    private val listeners = mutableListOf<ListenerDisposer>()

    init {
        addClass("wa-list-view")
    }

    fun addListeners() {
        skinProperty().onChangeWithDisposer {
            it?.let {
                try {
                    val scrollBar = virtualFlow().findChildren<ScrollBar>(true).singleOrNull { node ->
                        node.orientation == Orientation.VERTICAL
                    }
                    scrollBar?.valueProperty()?.onChangeWithDisposer {
                        val current = selectionModel.selectedIndex
                        val first = virtualFlow().firstVisibleCell?.index ?: 0
                        val last = virtualFlow().lastVisibleCell?.index ?: 0

                        if (current !in (first..last)) {
                            FX.eventbus.fire(StickyVerseChangedEvent(true))
                        } else {
                            FX.eventbus.fire(StickyVerseChangedEvent(false))
                        }
                    }?.also(listeners::add)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }.also(listeners::add)
    }

    fun removeListeners() {
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }
}

class StickyVerseChangedEvent(val showBanner: Boolean) : FXEvent()

fun <T> EventTarget.narrationTextListview(
    values: ObservableList<T>?,
    op: NarrationTextListView<T>.() -> Unit = {}
) = NarrationTextListView<T>().attachTo(this, op) {
        if (values is SortedFilteredList<T>) values.bindTo(it)
        else it.items = values
    }

fun <T> EventTarget.narrationTextListview(
    values: ObservableValue<ObservableList<T>>?,
    op: NarrationTextListView<T>.() -> Unit = {}
) = NarrationTextListView<T>().attachTo(this, op) {
        fun rebinder() {
            (it.items as? SortedFilteredList<T>)?.bindTo(it)
        }
        it.itemsProperty().bind(values)
        rebinder()
        it.itemsProperty().onChange {
            rebinder()
        }
    }
