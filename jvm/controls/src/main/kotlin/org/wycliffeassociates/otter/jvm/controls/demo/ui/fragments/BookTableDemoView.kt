package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import org.wycliffeassociates.otter.common.data.workbook.WorkbookStatus
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.controls.tableview.workbookTableView
import tornadofx.View
import tornadofx.observableListOf

class BookTableDemoView : View() {
    val workbookList = observableListOf<WorkbookStatus>(
        WorkbookStatus(0, "John", "", 0.3, null, true),
        WorkbookStatus(0, "Acts", "", 0.0, null, true),
        WorkbookStatus(0, "Genesis", "", 0.1, null, true),
        WorkbookStatus(0, "Leviticus", "", 0.5, null, false),
        WorkbookStatus(0, "Psalms", "", 0.8, null, false),
        WorkbookStatus(0, "Revelation", "", 1.0, null, false),
        WorkbookStatus(0, "Mark", "", 0.5, null, false),
        WorkbookStatus(0, "Malachi", "", 1.0, null, false),
        WorkbookStatus(0, "Proverbs", "", 0.2, null, true),
        WorkbookStatus(0, "Colossians", "", 1.0, null, true),
    )

    init {
        tryImportStylesheet("/css/popup-menu.css")
        tryImportStylesheet("/css/filtered-search-bar.css")
        tryImportStylesheet("/css/table-view.css")

        subscribeToWorkbookEvent()
    }

    private fun subscribeToWorkbookEvent() {
        workspace.subscribe<WorkbookOpenEvent> {
            val targetBook = it.data
            println("open ${targetBook.title}")
        }
        workspace.subscribe<WorkbookExportEvent> {
            val targetBook = it.data
            println("export ${targetBook.title}")
        }
        workspace.subscribe<WorkbookDeleteEvent> {
            val targetBook = it.data
            println("delete ${targetBook.title}")
        }
    }

    override val root = workbookTableView(workbookList)
}