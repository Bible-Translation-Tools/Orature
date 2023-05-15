/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.dev

import org.wycliffeassociates.otter.common.data.primitives.Anthology
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.workbookTableView
import java.time.LocalDateTime
import tornadofx.*

class WorkbookTableDemoView : View() {
    val workbookList = observableListOf<WorkbookDescriptor>(
        WorkbookDescriptor(0, "jhn",  "John", "", Anthology.NEW_TESTAMENT,0.3, LocalDateTime.now(), true),
        WorkbookDescriptor(0, "act", "Acts", "", Anthology.NEW_TESTAMENT,0.0, LocalDateTime.now(), true),
        WorkbookDescriptor(0, "gen", "Genesis", "", Anthology.OLD_TESTAMENT,0.1, LocalDateTime.now(), true),
        WorkbookDescriptor(0, "lev", "Leviticus", "", Anthology.OLD_TESTAMENT,0.5, LocalDateTime.now(), false),
        WorkbookDescriptor(0, "psa", "Psalms", "", Anthology.OLD_TESTAMENT,0.8, LocalDateTime.now(), false),
        WorkbookDescriptor(0, "rev", "Revelation", "", Anthology.NEW_TESTAMENT,1.0, LocalDateTime.now(), false),
        WorkbookDescriptor(0, "mrk", "Mark", "", Anthology.NEW_TESTAMENT,0.5, LocalDateTime.now(), false),
        WorkbookDescriptor(0, "mal", "Malachi", "", Anthology.OLD_TESTAMENT,1.0, LocalDateTime.now(), false),
        WorkbookDescriptor(0, "pro", "Proverbs", "", Anthology.OLD_TESTAMENT,0.2, LocalDateTime.now(), true),
        WorkbookDescriptor(0, "col", "Colossians", "", Anthology.NEW_TESTAMENT,1.0, LocalDateTime.now(), true),
        WorkbookDescriptor(0, "unknown", "Non-scripture", "", Anthology.OTHER,1.0, LocalDateTime.now(), true),
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

    override val root = vbox {
        paddingAll = 20.0
        workbookTableView(workbookList)
    }
}