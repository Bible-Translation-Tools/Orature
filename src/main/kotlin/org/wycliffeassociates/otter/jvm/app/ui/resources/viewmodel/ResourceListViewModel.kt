package org.wycliffeassociates.otter.jvm.app.ui.resources.viewmodel

import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.utils.mapNotNull
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.ResourceTabPaneViewModel
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.ResourceGroupCardItemList
import org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.model.resourceGroupCardItem
import org.wycliffeassociates.otter.jvm.utils.observeOnFxSafe
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class ResourceListViewModel : ViewModel() {
    internal val resourceTabPaneViewModel: ResourceTabPaneViewModel by inject()
    private val workbookViewModel: WorkbookViewModel by inject()

    val resourceGroupCardItemList: ResourceGroupCardItemList = ResourceGroupCardItemList()

    init {
        workbookViewModel.activeChapterProperty.onChangeAndDoNow { targetChapter ->
            targetChapter?.let {
                loadResourceGroups(getSourceChapter(targetChapter))
            }
        }
    }

    private fun getSourceChapter(targetChapter: Chapter): Chapter {
        return workbookViewModel.workbook.source.chapters.filter {
            it.title == targetChapter.title
        }.blockingFirst()
    }

    internal fun loadResourceGroups(chapter: Chapter) {
        chapter
            .children
            .startWith(chapter)
            .mapNotNull {
                resourceGroupCardItem(it, workbookViewModel.resourceSlug, onSelect = this::navigateToTakesPage)
            }
            .buffer(2) // Buffering by 2 prevents the list UI from jumping while groups are loading
            .observeOnFxSafe()
            .subscribe {
                resourceGroupCardItemList.addAll(it)
            }
    }

    internal fun navigateToTakesPage(bookElement: BookElement, resource: Resource) {
        // TODO use navigator to navigate to takes page
        workbookViewModel.activeChunkProperty.set(bookElement as? Chunk)
        resourceTabPaneViewModel.setRecordableListItems(
            listOfNotNull(resource.title, resource.body)
        )
    }
}