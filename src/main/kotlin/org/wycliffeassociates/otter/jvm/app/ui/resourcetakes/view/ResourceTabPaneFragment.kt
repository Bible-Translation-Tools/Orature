package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import org.wycliffeassociates.controls.ChromeableTabPane
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.ResourceTabPaneViewModel
import tornadofx.*
import org.wycliffeassociates.otter.jvm.utils.getNotNull

class ResourceTabPaneFragment : Fragment() {
    private val viewModel: ResourceTabPaneViewModel by inject()
    private val tabPane = ChromeableTabPane()

    override val root = tabPane

    // The tabs will add or remove themselves from the tabPane when their view model's 'recordable' property changes
    @Suppress("unused")
    private val tabs: List<RecordableTab> = listOf(
        recordableTab(ContentType.TITLE, 0),
        recordableTab(ContentType.BODY, 1)
    )

    private fun recordableTab(contentType: ContentType, sort: Int) =
        RecordableTab(
            viewModel = viewModel.contentTypeToViewModelMap.getNotNull(contentType),
            parent = tabPane,
            sort = sort,
            onTabSelect = viewModel::onTabSelect
    )
}