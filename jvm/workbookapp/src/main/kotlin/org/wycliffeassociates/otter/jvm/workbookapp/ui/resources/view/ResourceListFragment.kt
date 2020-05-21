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
                labelText =
                    "${messages[workbookViewModel.chapter.label]} "
                        .plus("${workbookViewModel.chapter.title} ")
                        .plus("${messages["resources"]}")
                filterText = messages["hideCompleted"]
                workbookProgressProperty.bind(resourceListViewModel.completionProgressProperty)
            }
        )
        add(
            ResourceListView(resourceListViewModel.resourceGroupCardItemList).apply {
                whenDocked {
                    resourceListViewModel.selectedGroupCardItem.get()?.let {
                        scrollTo(it)
                        resourceListViewModel.selectedGroupCardItem.set(null)
                        resourceListViewModel.calculateCompletionProgress()
                    }
                }
            }
        )
    }
}