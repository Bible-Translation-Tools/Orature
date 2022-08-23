package org.wycliffeassociates.otter.jvm.controls.listview

import javafx.scene.control.MultipleSelectionModel
import tornadofx.*

class NoSelectionModel<T> : MultipleSelectionModel<T>() {
    override fun clearAndSelect(index: Int) { /*no op */ }

    override fun select(index: Int) { /*no op */ }

    override fun select(obj: T) { /*no op */ }

    override fun clearSelection(index: Int) { /*no op */ }

    override fun clearSelection() { /*no op */ }

    override fun isSelected(index: Int) = false

    override fun isEmpty() = true

    override fun selectPrevious() { /*no op */ }

    override fun selectNext() { /*no op */ }

    override fun selectFirst() { /*no op */ }

    override fun selectLast() { /*no op */ }

    override fun getSelectedIndices() = observableListOf<Int>()

    override fun getSelectedItems() = observableListOf<T>()

    override fun selectIndices(index: Int, vararg indices: Int) { /*no op */ }

    override fun selectAll() { /*no op */ }
}
