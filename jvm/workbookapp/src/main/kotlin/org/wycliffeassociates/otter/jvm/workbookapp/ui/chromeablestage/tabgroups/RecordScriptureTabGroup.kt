package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.AnimatedTab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordScriptureFragment
import tornadofx.*

class RecordScriptureTabGroup : TabGroup() {
    private val tab = RecordScriptureTab()

    private inner class RecordScriptureTab : AnimatedTab() {
        init {
            add(RecordScriptureFragment().root)
        }
    }

    override fun activate() {
        tabPane.tabs.add(tab)
    }
}