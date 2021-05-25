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
            if (it.list.isEmpty()) {
                filteredLanguages.predicate = queryPredicate.and { true }
            } else {
                regionPredicate = Predicate { language -> selectedRegions.contains(language.region) }
                filteredLanguages.predicate = regionPredicate.and(queryPredicate)
            }
        }

        searchQueryProperty.onChange { query ->
            when {
                query.isNullOrBlank() -> filteredLanguages.predicate = regionPredicate.and { true }
                else -> {
                    queryPredicate = Predicate { language ->
                        language.slug.startsWith(query, true)
                            .or(language.name.startsWith(query, true))
                            .or(language.anglicizedName.startsWith(query, true))
                    }
                    filteredLanguages.predicate = queryPredicate.and(regionPredicate)
                }
            }
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
                createMenuItem(it) { selected ->
                    when (selected) {
                        true -> selectedRegions.add(it)
                        else -> selectedRegions.remove(it)
                    }
                }
            }
        )
        items.add(createMenuSeparator(messages["display"]))
        items.add(
            createMenuItem(messages["anglicized"]) { selected ->
                anglicizedProperty.set(selected)
            }
        )
        menuItems.setAll(items)
    }

    private fun createMenuSeparator(label: String): MenuItem {
        return CustomMenuItem().apply {
            styleClass.add("filtered-search-bar__menu__separator")
            content = Label(label)
            isHideOnClick = false
        }
    }

    private fun createMenuItem(label: String, onChecked: (Boolean) -> Unit): MenuItem {
        return CustomMenuItem().apply {
            content = CheckboxButton().apply {
                text = label
                selectedProperty().onChange {
                    onChecked(it)
                }
            }
            isHideOnClick = false
        }
    }
}
