package org.wycliffeassociates.otter.jvm.app

import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.ProjectCreationWizard
import tornadofx.*

class MyApp : App(Workspace::class) {
    init {
        workspace.header.removeFromParent()
    }
    override fun onBeforeShow(view:UIComponent) {
        workspace.dock<ProjectCreationWizard>()

    }
}
//launch the org.wycliffeassociates.otter.jvm.app
fun main(args: Array<String>) {
    launch<MyApp>(args)
}