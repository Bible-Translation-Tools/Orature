package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.languageTableView
import tornadofx.*

class LanguageTableDemoView : View() {
    private val languages =
        observableListOf(
            Language("en", "English", "English", "", true, ""),
            Language("fr", "français", "French", "", true, ""),
        )

    init {
        tryImportStylesheet("/css/popup-menu.css")
        tryImportStylesheet("/css/filtered-search-bar.css")
        tryImportStylesheet("/css/table-view.css")

        workspace.subscribe<LanguageSelectedEvent> {
            println("selected: ${it.item}")
        }
    }

    override val root =
        vbox {
            paddingAll = 20.0

            languageTableView(languages)
        }
}
