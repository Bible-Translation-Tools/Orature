package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class OQuAWorkspace : Workspace() {
    init {
        javaClass.getResource("/css/oqua.css")?.let {
            tryImportStylesheet(it.toExternalForm())
        }
    }

    override fun onBeforeShow() {
        super.onDock()
        workspace.dock(find<HomeView>())

        workspace.header.replaceWith(find<NavBar>().root)
    }
}