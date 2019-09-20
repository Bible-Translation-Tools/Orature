package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view

import javafx.scene.control.Tab
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel.RecordableTabViewModel
import tornadofx.*

class RecordableTab(
    private val viewModel: RecordableTabViewModel,
    private val onTabSelect: (Recordable) -> Unit
) : Tab() {

    init {
        textProperty().bind(viewModel.labelProperty)

        RecordResourceFragment(viewModel).apply {
            formattedTextProperty.bind(viewModel.getFormattedTextBinding())
            this@RecordableTab.content = this.root
        }

        selectedProperty().onChange { selected ->
            if (selected) {
                callOnTabSelect()
            }
        }
    }

    fun hasRecordable(): Boolean = viewModel.recordable != null

    private fun callOnTabSelect() {
        viewModel.recordable?.let { onTabSelect(it) }
            ?: throw IllegalStateException("Selected tab's recordable is null")
    }
}