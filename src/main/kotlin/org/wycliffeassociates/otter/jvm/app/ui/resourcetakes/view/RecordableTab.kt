package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.RecordableTabViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import kotlin.math.min

class RecordableTab(
    private val viewModel: RecordableTabViewModel,
    // tabPaneProperty gets set to null every time the tab gets removed from the tab pane so we need to cache it
    private val parent: TabPane,
    val sort: Int,
    private val onTabSelect: (Recordable) -> Unit
): Tab() {

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

        viewModel.recordableProperty.onChangeAndDoNow { item ->
            item?.let {
                checkAndAddSelf()
            } ?: removeSelf()
        }
    }

    private fun checkAndAddSelf() {
        if (!parent.tabs.contains(this)) {
            addSelfToParent()
        }
    }

    private fun removeSelf() {
        parent.tabs.remove(this)
    }

    private fun addSelfToParent() {
        parent.tabs.add(min(sort, parent.tabs.size), this)
        if (parent.tabs.size == 1) {
            selectTab()
        }
    }

    private fun selectTab() {
        select()
        callOnTabSelect()
    }

    private fun callOnTabSelect() {
        viewModel.recordable?.let { onTabSelect(it) }
            ?: throw IllegalStateException("Selected tab's recordable is null")
    }
}