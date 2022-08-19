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
