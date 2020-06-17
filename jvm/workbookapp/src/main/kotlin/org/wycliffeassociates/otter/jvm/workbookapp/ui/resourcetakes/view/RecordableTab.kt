package org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.view

import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.controls.statusindicator.StatusIndicator
import org.wycliffeassociates.otter.jvm.controls.statusindicator.statusindicator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableTab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel.RecordableTabViewModel
import tornadofx.*

class RecordableTab(
    private val viewModel: RecordableTabViewModel,
    private val onTabSelect: (Recordable) -> Unit
) : ChromeableTab() {

    private val mainContent = RecordResourceFragment(viewModel)

    init {
        textProperty().bind(viewModel.labelProperty)

        graphic = statusindicator {
            initForRecordableTab()
            progressProperty.bind(viewModel.getProgressBinding())
        }

        mainContent.apply {
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

    private fun StatusIndicator.initForRecordableTab() {
        prefWidth = 50.0
        primaryFill = Color.ORANGE
        accentFill = Color.LIGHTGRAY
        trackFill = Color.LIGHTGRAY
        indicatorRadius = 4.0
    }

    override fun setAnimatedContent() {
        animatedContent = mainContent.container
    }
}