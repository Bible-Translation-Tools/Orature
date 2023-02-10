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
import org.wycliffeassociates.otter.common.data.primitives.Verse
import org.wycliffeassociates.otter.jvm.utils.findChildren
import org.wycliffeassociates.otter.jvm.utils.virtualFlow
import tornadofx.*

class NarrationListView(items: ObservableList<Verse>? = null) : ListView<Verse>(items) {
    val selectedVerseLabelProperty = SimpleStringProperty()
    val onSelectedVerseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("wa-list-view")

        setCellFactory { NarrationVerseCell() }

        itemsProperty().onChange {
            selectionModel.select(0)
        }

        skinProperty().onChange {
            it?.let {
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
            }
        }
    }
}

fun EventTarget.narrationlistview(values: ObservableList<Verse>?, op: NarrationListView.() -> Unit = {}) =
    NarrationListView().attachTo(this, op) {
        if (values is SortedFilteredList<Verse>) values.bindTo(it)
        else it.items = values
}

fun EventTarget.narrationlistview(values: ObservableValue<ObservableList<Verse>>?, op: NarrationListView.() -> Unit = {}) =
    NarrationListView().attachTo(this, op) {
    fun rebinder() {
        (it.items as? SortedFilteredList<Verse>)?.bindTo(it)
    }
    it.itemsProperty().bind(values)
    rebinder()
    it.itemsProperty().onChange {
        rebinder()
    }
}