package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import java.util.EnumMap
import javafx.collections.ListChangeListener
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordResourceViewModel : ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    private var activeRecordable: Recordable? = null

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

        workbookViewModel.activeResourceMetadataProperty.onChangeAndDoNow { metadata ->
            metadata?.let { setTabLabels(metadata.identifier) }
        }
    }

    fun onTabSelect(recordable: Recordable) {
        activeRecordable = recordable
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
}