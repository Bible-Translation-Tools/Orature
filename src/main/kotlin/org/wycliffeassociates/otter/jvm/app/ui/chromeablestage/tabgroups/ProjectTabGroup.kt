package org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.jvm.app.ui.projectgrid.view.ProjectGridFragment
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
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
