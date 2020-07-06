package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import javafx.scene.control.Tab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordScriptureFragment
import tornadofx.*

class RecordScriptureTabGroup : TabGroup() {
    private val tab = Tab().apply {
        add(RecordScriptureFragment().root)
    }

    override fun activate() {
        tabPane.tabs.add(tab)
    }
}
