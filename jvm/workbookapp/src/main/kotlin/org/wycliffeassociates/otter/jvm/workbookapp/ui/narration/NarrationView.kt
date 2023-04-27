package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import tornadofx.*

class NarrationView() : View() {
    override val root = stackpane {
        borderpane {
            top<NarrationHeader>()
        }
    }
}