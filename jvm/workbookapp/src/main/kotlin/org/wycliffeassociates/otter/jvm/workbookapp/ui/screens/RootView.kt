package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.DeprecatedNavBar
import tornadofx.*

class RootView : View() {
    override val root = stackpane {
        borderpane {
            left<DeprecatedNavBar>()
            right<Workspace>()
        }
    }
}
