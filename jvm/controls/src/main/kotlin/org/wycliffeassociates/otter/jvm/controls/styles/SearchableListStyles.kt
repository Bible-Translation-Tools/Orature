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
package org.wycliffeassociates.otter.jvm.controls.styles

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
