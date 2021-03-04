package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import com.jfoenix.controls.JFXTabPane
import javafx.scene.Parent
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.jvm.utils.getNotNull
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecordResourceViewModel
import tornadofx.Fragment

class RecordResourcePage: Fragment() {
    private val viewModel: RecordResourceViewModel by inject()

    val tabPane = JFXTabPane()

    override val root = tabPane

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

    override fun onDock() {
        tabs.forEach { recordableTab ->
            recordableTab.bindProperties()
            recordableTab.recordableProperty.onChangeAndDoNow { rec ->
                rec?.let {
                    if (!tabPane.tabs.contains(recordableTab)) tabPane.tabs.add(recordableTab)
                } ?: tabPane.tabs.remove(recordableTab)
            }
        }
    }

    override fun onUndock() {
        tabs.forEach { recordableTab ->
            recordableTab.unbindProperties()
        }
    }
}
