package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ProjectListCellFragment: ListCellFragment<Workbook>() {
    private val wbDataStore: WorkbookDataStore by inject()

    private val projectProperty = stringBinding(itemProperty) {
        this.value?.source?.title
    }

    override val root = vbox {
        button (projectProperty) {
            action {
                wbDataStore.activeWorkbookProperty.set(item)
            }
        }
    }
}