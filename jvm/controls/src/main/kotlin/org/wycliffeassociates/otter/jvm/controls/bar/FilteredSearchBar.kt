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
package org.wycliffeassociates.otter.jvm.controls.bar

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.MenuItem
import javafx.scene.control.Skin
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.skins.bar.FilteredSearchBarSkin
import tornadofx.*

class FilteredSearchBar : Control() {

    val textProperty = SimpleStringProperty()
    val leftIconProperty = SimpleObjectProperty<Node>()
    val rightIconProperty = SimpleObjectProperty<Node>(FontIcon(MaterialDesign.MDI_MAGNIFY))
    val promptTextProperty = SimpleStringProperty()
    val filterItems: ObservableList<MenuItem> = observableListOf()

    init {
        styleClass.setAll("filtered-search-bar")
    }

    override fun createDefaultSkin(): Skin<*> {
        return FilteredSearchBarSkin(this)
    }
}
