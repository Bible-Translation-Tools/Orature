/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.skins.bar

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.MenuButton
import javafx.scene.control.SkinBase
import org.controlsfx.control.textfield.CustomTextField
import org.wycliffeassociates.otter.jvm.controls.bar.FilteredSearchBar
import tornadofx.*

class FilteredSearchBarSkin(private val bar: FilteredSearchBar) : SkinBase<FilteredSearchBar>(bar) {

    @FXML
    lateinit var searchField: CustomTextField

    @FXML
    lateinit var filterMenu: MenuButton

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        searchField.leftProperty().bind(bar.leftIconProperty)
        searchField.rightProperty().bind(bar.rightIconProperty)
        searchField.promptTextProperty().set(bar.promptTextProperty.value)

        filterMenu.items.bind(bar.filterItems) { it }

        bar.textProperty.bindBidirectional(searchField.textProperty())
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("FilteredSearchBar.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
