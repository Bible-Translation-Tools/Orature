package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration

import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class NarrationView : View() {
    private val header = find<NarrationHeader>()
    private val footer = find<NarrationFooter>()

    init {
        tryImportStylesheet(resources["/css/narration.css"])
        tryImportStylesheet(resources["/css/chapter-selector.css"])
    }

    override val root = stackpane {
        addClass(org.wycliffeassociates.otter.common.data.ColorTheme.LIGHT.styleClass)

        borderpane {
            top = header.root
            bottom = footer.root
        }
    }

    override fun onDock() {
        super.onDock()
        header.onDock()
        footer.onDock()
    }

    override fun onUndock() {
        super.onUndock()
        header.onUndock()
        footer.onUndock()
    }
}