package org.wycliffeassociates.otter.jvm.workbookapp.ui.resources.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.transformation.FilteredList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.utils.mapNotNull
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceGroupCardItemList
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.resourceGroupCardItem
import org.wycliffeassociates.otter.jvm.utils.observeOnFxSafe
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceGroupCardItem
import tornadofx.*

class ResourceListViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ResourceListViewModel::class.java)

    internal val recordResourceViewModel: RecordResourceViewModel by inject()
    private val workbookViewModel: WorkbookViewModel by inject()

    var selectedGroupCardItem = SimpleObjectProperty<ResourceGroupCardItem>()
    val resourceGroupCardItemList: ResourceGroupCardItemList = ResourceGroupCardItemList()
    val filteredResourceGroupCardItemList = FilteredList(resourceGroupCardItemList)

    val completionProgressProperty = SimpleDoubleProperty(0.0)
    val isFilterOnProperty = SimpleBooleanProperty(false)

    init {
        workbookViewModel.activeChapterProperty.onChangeAndDoNow {
            it?.let {
                loadResourceGroups(
                    workbookViewModel.getSourceChapter().blockingGet()
                )
            }
        }
        isFilterOnProperty.onChange { checked ->
            if (checked) {
                filteredResourceGroupCardItemList.setPredicate {
                    it.groupCompletedBinding().get().not()
                }
            } else {
                filteredResourceGroupCardItemList.predicate = null
            }
        }
    }

    private fun setSelectedResourceGroupCardItem(resource: Resource) {
        resourceGroupCardItemList.forEach { groupCardItem ->
            groupCardItem.resources
                .filter { it.resource == resource }
                .singleElement()
                .doOnError { e ->
                    logger.error("Error in setting selected resource group card item for: $resource", e)
                }
                .subscribe {
                    selectedGroupCardItem.set(groupCardItem)
                }
        }
    }

    internal fun loadResourceGroups(chapter: Chapter) {
        resourceGroupCardItemList.clear()
        chapter
            .children
            .startWith(chapter)
            .mapNotNull { bookElement ->
                resourceGroupCardItem(
                    element = bookElement,
                    slug = workbookViewModel.activeResourceMetadata.identifier,
                    onSelect = this::setActiveChunkAndRecordables
                )
            }
            .buffer(2) // Buffering by 2 prevents the list UI from jumping while groups are loading
            .observeOnFxSafe()
            .doFinally {
                calculateCompletionProgress()
            }
            .doOnError { e ->
                logger.error("Error in loading resource groups for $chapter", e)
            }
            .subscribe {
                resourceGroupCardItemList.addAll(it)
            }
    }

    internal fun setActiveChunkAndRecordables(bookElement: BookElement?, resource: Resource) {
        workbookViewModel.activeChunkProperty.set(bookElement as? Chunk)
        workbookViewModel.activeResourceProperty.set(resource)
        recordResourceViewModel.setRecordableListItems(
            listOfNotNull(resource.title, resource.body)
        )
        setSelectedResourceGroupCardItem(resource)
    }

    fun calculateCompletionProgress() {
        var completed = 0.0
        var totalItems = 0

        resourceGroupCardItemList.forEach { item ->
            item.resources
                .toList()
                .doOnError { e ->
                    logger.error("Error in calculating resource completion progress", e)
                }
                .subscribe { list ->
                    list.forEach {
                        it.titleProgressProperty.get().let { progress ->
                            completed += progress
                            totalItems++
                        }
                        it.bodyProgressProperty?.get()?.let { progress ->
                            completed += progress
                            totalItems++
                        }
                        if (totalItems > 0) {
                            runLater { completionProgressProperty.set(completed / totalItems) }
                        }
                    }
                }
        }
    }
}
