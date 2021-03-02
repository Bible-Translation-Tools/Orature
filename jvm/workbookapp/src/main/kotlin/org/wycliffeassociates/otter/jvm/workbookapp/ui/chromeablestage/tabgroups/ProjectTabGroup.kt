package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import javafx.scene.control.Tab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ProjectHome
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookViewModel

class ProjectTabGroup : TabGroup() {
    private val workbookViewModel: WorkbookViewModel by inject()
    val projectGridFragment = find<ProjectHome>()

    override fun activate() {
        workbookViewModel.activeWorkbookProperty.set(null)

        tabPane.tabs.add(Tab("", projectGridFragment.root))
    }
}
