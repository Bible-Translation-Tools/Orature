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
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Orientation
import javafx.scene.control.ListView
import javafx.scene.control.ScrollBar
import org.wycliffeassociates.otter.jvm.utils.findChildren
import org.wycliffeassociates.otter.jvm.utils.virtualFlow
import tornadofx.*

class NarrationTextListView<T>(items: ObservableList<T>? = null) : ListView<T>(items) {
    val cardIsOutOfViewProperty = SimpleBooleanProperty()
    val onSelectedVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    val initialSelectedItemProperty = SimpleObjectProperty<T>()

    init {
        addClass("wa-list-view")

        initialSelectedItemProperty.onChange {
            cardIsOutOfViewProperty.set(false)
            selectionModel.select(it)
            scrollTo(it)
        }

        skinProperty().onChange {
            it?.let {
                try {
                    val scrollBar = virtualFlow().findChildren<ScrollBar>(true).singleOrNull { node ->
                        node.orientation == Orientation.VERTICAL
                    }
                    scrollBar?.valueProperty()?.onChange {
                        val current = selectionModel.selectedIndex
                        val first = virtualFlow().firstVisibleCell?.index ?: 0
                        val last = virtualFlow().lastVisibleCell?.index ?: 0

                        if (current !in (first..last)) {
                            cardIsOutOfViewProperty.set(true)
                            onSelectedVerseActionProperty.set(EventHandler {
                                selectionModel.select(current)
                                scrollTo(current)
                            })
                        } else {
                            cardIsOutOfViewProperty.set(false)
                            onSelectedVerseActionProperty.set(null)
                        }
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }
    }
}

fun <T> EventTarget.narrationtextlistview(values: ObservableList<T>?, op: NarrationTextListView<T>.() -> Unit = {}) =
    NarrationTextListView<T>().attachTo(this, op) {
        if (values is SortedFilteredList<T>) values.bindTo(it)
        else it.items = values
    }

fun <T> EventTarget.narrationtextlistview(
    values: ObservableValue<ObservableList<T>>?,
    op: NarrationTextListView<T>.() -> Unit = {}
) =
    NarrationTextListView<T>().attachTo(this, op) {
        fun rebinder() {
            (it.items as? SortedFilteredList<T>)?.bindTo(it)
        }
        it.itemsProperty().bind(values)
        rebinder()
        it.itemsProperty().onChange {
            rebinder()
        }
    }
