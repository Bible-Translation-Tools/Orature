package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.app.ui.takemanagement.viewmodel.TakeManagementViewModel
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import java.util.EnumMap
import javafx.collections.ListChangeListener
import tornadofx.*

class RecordResourceViewModel : ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()
    private val takeManagementViewModel: TakeManagementViewModel by inject()

    internal val recordableList: ObservableList<Recordable> = FXCollections.observableArrayList()

    private val activeRecordableProperty = SimpleObjectProperty<Recordable>()
    var activeRecordable: Recordable by activeRecordableProperty

    class ContentTypeToViewModelMap(map: Map<ContentType, RecordableTabViewModel>):
        EnumMap<ContentType, RecordableTabViewModel>(map)
    val contentTypeToViewModelMap = ContentTypeToViewModelMap(
        hashMapOf(
            ContentType.TITLE to RecordableTabViewModel(SimpleStringProperty()),
            ContentType.BODY to RecordableTabViewModel(SimpleStringProperty())
        )
    )

    init {
        initTabs()

        recordableList.onChange {
            updateRecordables(it)
        }

        setTabLabels(workbookViewModel.resourceSlug)
        workbookViewModel.activeResourceSlugProperty.onChange {
            setTabLabels(it)
        }
    }

    fun onTabSelect(recordable: Recordable) {
        activeRecordable = recordable
    }

    fun setRecordableListItems(items: List<Recordable>) {
        if (!recordableList.containsAll(items))
            recordableList.setAll(items)
    }

    fun newTakeAction() {
        takeManagementViewModel.recordNewTake(activeRecordable)
    }

    private fun initTabs() {
        recordableList.forEach {
            addRecordableToTabViewModel(it)
        }
    }

    private fun setTabLabels(resourceSlug: String?) {
        when(resourceSlug) {
            "tn" -> {
                setLabelProperty(ContentType.TITLE, messages["snippet"])
                setLabelProperty(ContentType.BODY, messages["note"])
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