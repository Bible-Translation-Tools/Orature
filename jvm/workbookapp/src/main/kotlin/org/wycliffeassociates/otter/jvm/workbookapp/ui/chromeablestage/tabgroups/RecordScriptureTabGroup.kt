package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.AnimatedTab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.AnimatedChromeableTabPane
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordScriptureFragment
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.RecordScriptureViewModel
import tornadofx.*

class RecordScriptureTabGroup : TabGroup() {
    private val tab = RecordScriptureTab()
    private val recordScriptureViewModel: RecordScriptureViewModel by inject()

    init {
        recordScriptureViewModel.transitionDirectionProperty.onChange {
            it?.let {
                (tabPane as? AnimatedChromeableTabPane)?.animate(it)
                recordScriptureViewModel.transitionDirectionProperty.set(null)
            }
        }
    }

    private inner class RecordScriptureTab : AnimatedTab() {
        init {
            add(RecordScriptureFragment().root)
        }
    }

    override fun activate() {
        tabPane.tabs.add(tab)
    }
}