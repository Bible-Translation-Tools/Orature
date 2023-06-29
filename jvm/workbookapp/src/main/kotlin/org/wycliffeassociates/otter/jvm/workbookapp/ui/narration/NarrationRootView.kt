package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import javafx.scene.layout.Priority
import tornadofx.*

class NarrationRootView : View() {
    override val root = borderpane {
        center<Workspace>()
    }

    init {
        workspace.header.removeFromParent()
        workspace.root.vgrow = Priority.ALWAYS
    }
}