package org.wycliffeassociates.otter.jvm.controls.listview

import javafx.collections.ObservableList
import javafx.scene.control.MultipleSelectionModel
import tornadofx.*

class NoSelectionModel<T> : MultipleSelectionModel<T>() {
    override fun clearAndSelect(index: Int) {
    }

    override fun select(index: Int) {
    }

    override fun select(obj: T) {
    }

    override fun clearSelection(index: Int) {
    }

    override fun clearSelection() {
    }

    override fun isSelected(index: Int): Boolean {
        return false
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun selectPrevious() {
        TODO("Not yet implemented")
    }

    override fun selectNext() {
    }

    override fun selectFirst() {
    }

    override fun selectLast() {
    }

    override fun getSelectedIndices(): ObservableList<Int> {
        return observableListOf()
    }

    override fun getSelectedItems(): ObservableList<T> {
        return observableListOf()
    }

    override fun selectIndices(index: Int, vararg indices: Int) {
        TODO("Not yet implemented")
    }

    override fun selectAll() {
        TODO("Not yet implemented")
    }
}
