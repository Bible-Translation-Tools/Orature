package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view.RecordableTab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.AnimatedChromeableTabPane
import tornadofx.*

class RecordResourceTabGroup : TabGroup() {
    private val viewModel: RecordResourceViewModel by inject()

    init {
        viewModel.transitionDirectionProperty.onChange {
            it?.let {
                (tabPane as? AnimatedChromeableTabPane)?.animate(it)
                viewModel.transitionDirectionProperty.set(null)
            }
        }
    }

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

    override fun activate() {
        tabs.forEach { recordableTab ->
            if (recordableTab.hasRecordable()) {
                tabPane.tabs.add(recordableTab)
            }
        }
    }
}