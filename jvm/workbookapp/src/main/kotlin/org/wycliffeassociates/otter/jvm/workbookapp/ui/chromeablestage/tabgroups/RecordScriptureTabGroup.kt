package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordScriptureFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordScriptureViewModel
import tornadofx.*

class RecordScriptureTabGroup : AnimatedTabGroup() {
    private val tab = RecordScriptureTab()
    private val viewModel: RecordScriptureViewModel by inject()

    init {
        viewModel.transitionDirectionProperty.onChange {
            it?.let {
                animate(it)
                viewModel.transitionDirectionProperty.set(null)
            }
        }
    }

    private inner class RecordScriptureTab : AnimatedTab() {
        init {
            add(RecordScriptureFragment().root)
        }
    }

    override fun activate() {
        super.activate()
        tabPane.tabs.add(tab)
    }
}