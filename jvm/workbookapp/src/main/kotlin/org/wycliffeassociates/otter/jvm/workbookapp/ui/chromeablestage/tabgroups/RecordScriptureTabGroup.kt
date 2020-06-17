package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableTab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordScriptureFragment
import tornadofx.*

class RecordScriptureTabGroup : TabGroup() {
    private val tab = RecordScriptureTab()

    private inner class RecordScriptureTab : ChromeableTab() {
        init {
            add(RecordScriptureFragment().root)
        }
    }

    override fun activate() {
        tabPane.tabs.add(tab)
    }
}