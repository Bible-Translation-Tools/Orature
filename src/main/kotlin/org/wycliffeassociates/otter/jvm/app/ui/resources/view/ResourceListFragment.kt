package org.wycliffeassociates.otter.jvm.app.ui.resources.view

import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenStyles
import org.wycliffeassociates.otter.jvm.app.widgets.workbookheader.workbookheader
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.styles.ResourceListStyles
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.view.ResourceListView
import org.wycliffeassociates.otter.jvm.app.ui.resources.viewmodel.ResourceListViewModel
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class ResourceListFragment : Fragment() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val resourceListViewModel: ResourceListViewModel by inject()

    init {
        importStylesheet<MainScreenStyles>()
        importStylesheet<ResourceListStyles>()
    }
    override val root = vbox {

        addClass(MainScreenStyles.main)

        add(
            workbookheader {
                labelText = "${workbookViewModel.chapter.title} ${messages["resources"]}"
                filterText = messages["hideCompleted"]
            }
        )
        add(
            ResourceListView(resourceListViewModel.resourceGroupCardItemList)
        )
    }
}