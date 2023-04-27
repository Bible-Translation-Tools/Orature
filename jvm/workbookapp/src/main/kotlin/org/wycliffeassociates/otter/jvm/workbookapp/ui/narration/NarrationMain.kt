package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import com.nhaarman.mockitokotlin2.mock
import javafx.stage.Stage
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class NarrationApp(): App(NarrationView::class) {

    // val workbookDataStore by inject<WorkbookDataStore>()

    init {
        // val workbook = mock<Workbook>()
        // workbookDataStore.activeWorkbookProperty.set(workbook)
    }


    override fun start(stage: Stage) {
        super.start(stage)
        stage.height = 600.0
        stage.width = 800.0
    }
}

fun main() {
    launch<NarrationApp>()
}