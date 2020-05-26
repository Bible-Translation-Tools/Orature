package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import javafx.scene.control.Tab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view.RecordScriptureFragment
import tornadofx.*

class RecordScriptureTabGroup : TabGroup() {
    override fun activate() {
        showHorizontalNavBarProperty.set(false)
        tabPane.tabs.add(
            Tab().apply {
                add(RecordScriptureFragment().root)
            }
        )
    }
}