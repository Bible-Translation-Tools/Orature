package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import org.wycliffeassociates.otter.jvm.controls.workbookheader.workbookheader
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.styles.ResourceListStyles
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view.ResourceListView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ResourceListViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*
import java.text.MessageFormat

class ResourceListFragment : Fragment() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val resourceListViewModel: ResourceListViewModel by inject()

    init {
        importStylesheet<ResourceListStyles>()
    }

    override val root = vbox {
        add(
            workbookheader {
                labelText = MessageFormat.format(
                    messages["chapterResourcesLabel"],
                    messages[workbookViewModel.chapter.label],
                    workbookViewModel.chapter.title,
                    messages["resources"]
                )
                filterText = messages["hideCompleted"]
                workbookProgressProperty.bind(resourceListViewModel.completionProgressProperty)
                resourceListViewModel.isFilterOnProperty.bind(isFilterOnProperty)
            }
        )
        add(
            ResourceListView(
                resourceListViewModel.filteredResourceGroupCardItemList,
                resourceListViewModel.isFilterOnProperty
            ).apply {
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
