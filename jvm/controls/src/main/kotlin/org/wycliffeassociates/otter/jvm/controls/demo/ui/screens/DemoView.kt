package org.wycliffeassociates.otter.jvm.controls.demo.ui.screens

import org.wycliffeassociates.otter.jvm.controls.demo.ui.components.ControlContent
import org.wycliffeassociates.otter.jvm.controls.demo.ui.components.ControlMenu
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.View
import tornadofx.borderpane

class DemoView : View() {

    override val root = borderpane {
        left<ControlMenu>()
        center<ControlContent>()
    }

    init {
        tryImportStylesheet(resources["/css/demo.css"])
    }
}