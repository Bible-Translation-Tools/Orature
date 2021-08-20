/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import java.util.EnumMap
import javafx.collections.ListChangeListener
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Resource
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceCardItem
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceGroupCardItem
import tornadofx.*
import java.text.MessageFormat

class RecordResourceViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(RecordResourceViewModel::class.java)

    val currentTakeNumberProperty = SimpleObjectProperty<Int?>()
    val breadcrumbTitleBinding = currentTakeNumberProperty.stringBinding {
        it?.let { take ->
            MessageFormat.format(
                messages["takeTitle"],
                messages["take"],
                take
            )
        } ?: messages["take"]
    }

    private enum class StepDirection {
        FORWARD,
        BACKWARD
    }

    private val workbookDataStore: WorkbookDataStore by inject()
    private val resourceListViewModel: ResourceListViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()
    private val recordableViewModel = RecordableViewModel(audioPluginViewModel)

    private val activeChunkProperty = SimpleObjectProperty<Chunk>()
    private val activeChunk: Chunk? by activeChunkProperty

    private val activeResourceProperty = SimpleObjectProperty<Resource>()
    private val activeResource: Resource? by activeResourceProperty

    private val resourceList: ObservableList<Resource> = observableListOf()

    val hasNext = SimpleBooleanProperty(false)
    val hasPrevious = SimpleBooleanProperty(false)

    private var activeResourceSubscription: Disposable? = null

    internal val recordableList: ObservableList<Recordable> = FXCollections.observableArrayList()

    class ContentTypeToViewModelMap(map: Map<ContentType, RecordableTabViewModel>) :
        EnumMap<ContentType, RecordableTabViewModel>(map)

    val contentTypeToViewModelMap = ContentTypeToViewModelMap(
        hashMapOf(
            ContentType.TITLE to tabRecordableViewModel(),
            ContentType.BODY to tabRecordableViewModel()
        )
    )

    private fun tabRecordableViewModel() =
        RecordableTabViewModel(SimpleStringProperty(), audioPluginViewModel)

    init {
        initTabs()

        recordableList.onChange {
            updateRecordables(it)
        }

        workbookDataStore.activeResourceMetadataProperty.onChangeAndDoNow { metadata ->
            metadata?.let { setTabLabels(metadata.identifier) }
        }

        workbookDataStore.activeChapterProperty.onChangeAndDoNow { chapter ->
            chapter?.let {
                if (activeChunkProperty.value == null) {
                    setHasNextAndPrevious()
                }
            }
        }

        resourceListViewModel.selectedGroupCardItem.onChangeAndDoNow { item ->
            item?.let {
                getResourceList(it.resources)
                setHasNextAndPrevious()
            }
        }

        workbookDataStore.activeChunkProperty.onChangeAndDoNow {
            activeChunkProperty.set(it)
        }

        workbookDataStore.activeResourceProperty.onChangeAndDoNow {
            activeResourceProperty.set(it)
            setHasNextAndPrevious()
        }
    }

    fun onTabSelect(recordable: Recordable) {
        workbookDataStore.activeResourceComponentProperty.set(recordable as Resource.Component)
    }

    fun setRecordableListItems(items: List<Recordable>) {
        if (!recordableList.containsAll(items))
            recordableList.setAll(items)
    }

    private fun initTabs() {
        recordableList.forEach {
            addRecordableToTabViewModel(it)
        }
    }

    private fun setTabLabels(resourceSlug: String?) {
        when (resourceSlug) {
            "tn" -> {
                setLabelProperty(ContentType.TITLE, messages["snippet"])
                setLabelProperty(ContentType.BODY, messages["note"])
            }
            "tq" -> {
                setLabelProperty(ContentType.TITLE, messages["question"])
                setLabelProperty(ContentType.BODY, messages["answer"])
            }
        }
    }

    private fun setLabelProperty(contentType: ContentType, label: String) {
        contentTypeToViewModelMap.getNotNull(contentType).labelProperty.set(label)
    }

    private fun updateRecordables(change: ListChangeListener.Change<out Recordable>) {
        while (change.next()) {
            change.removed.forEach { recordable ->
                removeRecordableFromTabViewModel(recordable)
            }
            change.addedSubList.forEach { recordable ->
                addRecordableToTabViewModel(recordable)
            }
        }
    }

    private fun addRecordableToTabViewModel(item: Recordable) {
        contentTypeToViewModelMap.getNotNull(item.contentType).recordable = item
    }

    private fun removeRecordableFromTabViewModel(item: Recordable) {
        contentTypeToViewModelMap.getNotNull(item.contentType).recordable = null
    }

    fun nextChunk() {
        stepToChunk(StepDirection.FORWARD)
    }

    fun previousChunk() {
        stepToChunk(StepDirection.BACKWARD)
    }

    private fun stepToChunk(direction: StepDirection) {
        when (direction) {
            StepDirection.FORWARD -> {
                nextResource()?.let {
                    resourceListViewModel.setActiveChunkAndRecordables(activeChunk, it)
                } ?: run {
                    nextGroupCardItem()?.let { nextItem ->
                        nextItem.resources
                            .firstElement()
                            .doOnError { e ->
                                logger.error("Error in step to chunk, direction: $direction", e)
                            }
                            .subscribe {
                                resourceListViewModel.setActiveChunkAndRecordables(nextItem.bookElement, it.resource)
                            }
                    }
                }
            }
            StepDirection.BACKWARD -> {
                previousResource()?.let {
                    resourceListViewModel.setActiveChunkAndRecordables(activeChunk, it)
                } ?: run {
                    previousGroupCardItem()?.let { previousItem ->
                        previousItem.resources
                            .lastElement()
                            .doOnError { e ->
                                logger.error("Error in step to chunk, direction, $direction", e)
                            }
                            .subscribe {
                                resourceListViewModel.setActiveChunkAndRecordables(
                                    previousItem.bookElement,
                                    it.resource
                                )
                            }
                    }
                }
            }
        }
    }

    private fun setHasNextAndPrevious() {
        hasNext.set(nextResource() != null || nextGroupCardItem() != null)
        hasPrevious.set(previousResource() != null || previousGroupCardItem() != null)
    }

    private fun getResourceList(resources: Observable<ResourceCardItem>) {
        resourceList.clear()
        activeResourceSubscription?.dispose()
        activeResourceSubscription = resources
            .toList()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in get resource list", e)
            }
            .subscribe { list ->
                list.forEach {
                    resourceList.add(it.resource)
                }
            }
    }

    private fun nextResource(): Resource? {
        var currentResourceIndex = resourceList.indexOf(activeResource)
        return resourceList.getOrNull(currentResourceIndex + 1)
    }

    private fun previousResource(): Resource? {
        var currentResourceIndex = resourceList.indexOf(activeResource)
        return resourceList.getOrNull(currentResourceIndex - 1)
    }

    private fun nextGroupCardItem(): ResourceGroupCardItem? {
        var currentGroupCardItemIndex = resourceListViewModel.resourceGroupCardItemList.indexOf(
            resourceListViewModel.selectedGroupCardItem.get()
        )
        return resourceListViewModel.resourceGroupCardItemList.getOrNull(
            currentGroupCardItemIndex + 1
        )
    }

    private fun previousGroupCardItem(): ResourceGroupCardItem? {
        var currentGroupCardItemIndex = resourceListViewModel.resourceGroupCardItemList.indexOf(
            resourceListViewModel.selectedGroupCardItem.get()
        )
        return resourceListViewModel.resourceGroupCardItemList.getOrNull(
            currentGroupCardItemIndex - 1
        )
    }
}
