package org.wycliffeassociates.otter.jvm.workbookapp.ui.resources.view

import org.wycliffeassociates.otter.jvm.controls.workbookheader.workbookheader
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.styles.ResourceListStyles
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view.ResourceListView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resources.viewmodel.ResourceListViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class ResourceListFragment : Fragment() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val resourceListViewModel: ResourceListViewModel by inject()

    init {
        importStylesheet<ResourceListStyles>()
    }
    override val root = vbox {
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