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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.button.CheckboxButton
import tornadofx.*
import java.util.function.Predicate

class LanguageSelectionViewModel(items: ObservableList<Language>) : ViewModel() {

    val searchQueryProperty = SimpleStringProperty("")
    val regions = observableListOf<String>()
    private val selectedRegions = observableListOf<String>()

    val menuItems = observableListOf<MenuItem>()
    val anglicizedProperty = SimpleBooleanProperty(false)

    val filteredLanguages = FilteredList(items)

    private var regionPredicate = Predicate<Language> { true }
    private var queryPredicate = Predicate<Language> { true }

    init {
        selectedRegions.onChange {
            regionPredicate = if (it.list.isEmpty()) {
                Predicate { false }
            } else {
                Predicate { language -> selectedRegions.contains(language.region) }
            }
            filteredLanguages.predicate = regionPredicate.and(queryPredicate)
        }

        searchQueryProperty.onChange { query ->
            queryPredicate = if(query.isNullOrBlank()) {
                Predicate { true }
            } else {
                Predicate { language ->
                    language.slug.contains(query, true)
                        .or(language.name.contains(query, true))
                        .or(language.anglicizedName.contains(query, true))
                }
            }
            filteredLanguages.predicate = queryPredicate.and(regionPredicate)
        }
    }

    fun resetFilter() {
        regionPredicate = Predicate { true }
        queryPredicate = Predicate { true }
        searchQueryProperty.set("")
        regions.clear()
        selectedRegions.clear()
        anglicizedProperty.set(false)
    }

    fun setFilterMenu() {
        val items = mutableListOf<MenuItem>()
        items.add(createMenuSeparator(messages["region"]))
        items.addAll(
            regions.map {
                val title = it.ifBlank { messages["unknown"] }
                createMenuItem(title, true) { selected ->
                    when (selected) {
                        true -> selectedRegions.add(it)
                        else -> selectedRegions.remove(it)
                    }
                }
            }
        )
        items.add(createMenuSeparator(messages["display"]))
        items.add(
            createMenuItem(messages["anglicized"], false) { selected ->
                anglicizedProperty.set(selected)
            }
        )
        menuItems.setAll(items)
    }

    private fun createMenuSeparator(label: String): MenuItem {
        return CustomMenuItem().apply {
            styleClass.add("wa-menu-button__separator")
            content = Label(label)
            isHideOnClick = false
        }
    }

    private fun createMenuItem(
        label: String,
        preSelected: Boolean,
        onChecked: (Boolean) -> Unit
    ): MenuItem {
        return CustomMenuItem().apply {
            content = CheckboxButton().apply {
                text = label
                selectedProperty().onChange {
                    onChecked(it)
                }
                isSelected = preSelected
            }
            isHideOnClick = false
        }
    }
}
