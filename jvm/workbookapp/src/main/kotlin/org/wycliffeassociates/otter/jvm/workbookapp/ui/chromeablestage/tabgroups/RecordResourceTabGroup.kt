package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.jvm.controls.resourcenavbar.ResourceNavBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view.RecordableTab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class RecordResourceTabGroup : TabGroup() {
    private val viewModel: RecordResourceViewModel by inject()
    private val resourceNavBar = ResourceNavBar()

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
        viewModel.resourceNavBarVisibleProperty.set(true)
        tabs.forEach { recordableTab ->
            recordableTab.bindProperties()
            recordableTab.recordableProperty.onChangeAndDoNow { rec ->
                rec?.let {
                    if (!tabPane.tabs.contains(recordableTab)) tabPane.tabs.add(recordableTab)
                } ?: tabPane.tabs.remove(recordableTab)
            }
        }
    }

    override fun deactivate() {
        viewModel.resourceNavBarVisibleProperty.set(false)
        tabs.forEach { recordableTab ->
            recordableTab.unbindProperties()
        }
    }

    init {
        root.apply {
            resourceNavBar.apply {
                visibleWhen { viewModel.resourceNavBarVisibleProperty }
                managedWhen { visibleProperty() }
                prefHeight = 70.0

                previousButtonTextProperty().set(messages["previousChunk"])
                nextButtonTextProperty().set(messages["nextChunk"])

                hasPreviousProperty().bind(viewModel.hasPrevious)
                hasNextProperty().bind(viewModel.hasNext)

                onPreviousAction { viewModel.previousChunk() }
                onNextAction { viewModel.nextChunk() }

                importStylesheet(userAgentStylesheet)
            }
            add(resourceNavBar)
        }
    }
}