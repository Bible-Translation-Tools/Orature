package org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.tabgroups

import javafx.scene.control.Tab
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.RecordScriptureFragment
import tornadofx.*

class RecordScriptureTabGroup : TabGroup() {

    var fragment: RecordScriptureFragment? = null

    private val tab = Tab().apply {
        fragment = find()
        add(fragment!!.root)
    }

    override fun activate() {
        fragment?.onDock()
        tabPane.tabs.add(tab)
    }

    override fun deactivate() {
        fragment?.onUndock()
    }
}
