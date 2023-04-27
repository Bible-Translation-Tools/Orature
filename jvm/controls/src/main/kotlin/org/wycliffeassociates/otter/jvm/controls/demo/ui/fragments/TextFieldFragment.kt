package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import org.wycliffeassociates.otter.jvm.controls.bar.searchBar
import org.wycliffeassociates.otter.jvm.controls.demo.ui.components.HeaderWithSearchBar
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class TextFieldFragment : View() {
    init {
        tryImportStylesheet("/css/popup-menu.css")
        tryImportStylesheet("/css/filtered-search-bar.css")
    }

    override val root = vbox {
        searchBar()
    }
}