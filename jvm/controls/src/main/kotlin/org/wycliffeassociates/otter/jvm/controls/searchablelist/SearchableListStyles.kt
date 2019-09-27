package org.wycliffeassociates.otter.jvm.controls.searchablelist

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.px

class SearchableListStyles : Stylesheet() {
    companion object {
        val searchableList by cssclass("wa-searchable-list")
        val searchFieldContainer by cssclass("wa-search-field-container")
        val searchField by cssclass("wa-search-field")
        val searchListView by cssclass("wa-search-list-view")
        val icon by cssclass("wa-searchable-list-icon")

        fun searchIcon(size: String = "1em") = MaterialIconView(MaterialIcon.SEARCH, size)
    }

    init {
        searchableList {
            spacing = 30.px
            searchFieldContainer {
                alignment = Pos.CENTER_LEFT
            }
        }
    }
}