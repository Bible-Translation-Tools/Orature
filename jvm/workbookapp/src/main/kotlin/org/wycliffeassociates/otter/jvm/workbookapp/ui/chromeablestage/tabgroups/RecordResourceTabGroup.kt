package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import javafx.beans.property.SimpleBooleanProperty
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view.RecordableTab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordResourceTabGroup : TabGroup() {
    private val viewModel: RecordResourceViewModel by inject()

    private val tabs: List<RecordableTab> = listOf(
        recordableTab(ContentType.TITLE),
        recordableTab(ContentType.BODY)
    )

    private fun recordableTab(contentType: ContentType): RecordableTab {
        return RecordableTab(
            viewModel = viewModel.contentTypeToViewModelMap.getNotNull(contentType),
            onTabSelect = viewModel::onTabSelect
        )
    }

    private fun initTabs() {
        tabPane.tabs.clear()
        tabs.forEach { recordableTab ->
            if (recordableTab.hasRecordable()) {
                tabPane.tabs.add(recordableTab)
            }
        }
    }

    override fun activate() {
        resourceNavBarVisibleProperty.set(true)
        initTabs()
        viewModel.resourceChangedProperty.onChange {
            if (it) {
                initTabs()
            }
        }
    }
}