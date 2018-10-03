package org.wycliffeassociates.otter.jvm.app

import org.wycliffeassociates.otter.common.domain.ImportLanguages
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.menu.MainMenu
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.ProjectCreationWizard
import tornadofx.*
import java.io.File

class MyApp : App(Workspace::class) {
    init {
        workspace.header.removeFromParent()
        workspace.add(MainMenu())
    }
    override fun onBeforeShow(view:UIComponent) {
        workspace.dock<ProjectCreationWizard>()

    }
}
//launch the org.wycliffeassociates.otter.jvm.app
fun main(args: Array<String>) {
    ImportLanguages(
            File(ClassLoader.getSystemResource("langnames.json").toURI()),
            Injector.languageRepo
    ).import().subscribe()
    launch<MyApp>(args)
}