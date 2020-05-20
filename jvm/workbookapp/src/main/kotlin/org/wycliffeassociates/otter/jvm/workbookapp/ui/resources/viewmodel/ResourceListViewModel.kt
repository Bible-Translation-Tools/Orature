package org.wycliffeassociates.otter.jvm.workbookapp.ui.resources.viewmodel

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
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
    internal val recordResourceViewModel: RecordResourceViewModel by inject()
    private val workbookViewModel: WorkbookViewModel by inject()

    var selectedGroupCardItem = SimpleObjectProperty<ResourceGroupCardItem>()
    val resourceGroupCardItemList: ResourceGroupCardItemList = ResourceGroupCardItemList()

    val workbookProgressProperty = SimpleDoubleProperty(0.0)

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

    private fun setSelectedResourceGroupCardItem(resource: Resource) {
        resourceGroupCardItemList.forEach { groupCardItem ->
            groupCardItem.resources
                .filter { it.resource == resource }
                .singleElement()
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
                addChangeListeners()
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

    fun addChangeListeners() {
        resourceGroupCardItemList.forEach { item ->
            item.resources
                .toList()
                .subscribe { list ->
                    list.forEach {
                        it.titleProgressProperty.onChangeAndDoNow {
                            calculateWorkbookProgress()
                        }
                        it.bodyProgressProperty?.onChangeAndDoNow {
                            calculateWorkbookProgress()
                        }
                    }
                }
        }
    }

    fun calculateWorkbookProgress() {
        var completed = 0.0
        var totalItems = 0

        resourceGroupCardItemList.forEach { item ->
            item.resources
                .toList()
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
                            runLater { workbookProgressProperty.set(completed / totalItems) }
                        }
                    }
                }
        }
    }
}