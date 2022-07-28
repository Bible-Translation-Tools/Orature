package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.beans.binding.Bindings
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ProjectListCellFragment: ListCellFragment<Workbook>() {
    private val wbDataStore: WorkbookDataStore by inject()

    private val projectProperty = Bindings.createStringBinding(
        { itemProperty.value?.source?.title },
        itemProperty
    )

    override val root = vbox {
        button (projectProperty) {
            action {
                wbDataStore.activeWorkbookProperty.set(item)
                workspace.dock(find<ProjectView>())
            }
        }
    }
}