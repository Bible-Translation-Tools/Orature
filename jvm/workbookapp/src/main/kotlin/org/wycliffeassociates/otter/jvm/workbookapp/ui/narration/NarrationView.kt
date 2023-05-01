package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class NarrationView : View() {
    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])
    }

    override val root = stackpane {
        borderpane {
            top<NarrationHeader>()
            bottom<NarrationFooter>()
        }
    }
}