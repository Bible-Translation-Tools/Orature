package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ProjectGridFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class ProjectTabGroup : TabGroup() {
    private val workbookViewModel: WorkbookViewModel by inject()

    override fun activate() {
        workbookViewModel.activeWorkbookProperty.set(null)

        tabPane.apply {
            tab<ProjectGridFragment> {}
        }
    }
}
