package org.wycliffeassociates.otter.jvm.app

import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.plugins.InitializePlugins
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.menu.view.MainMenu
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.view.ProjectHomeView
import org.wycliffeassociates.otter.jvm.app.ui.AppStyles
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.ProgressDialogStyles
import tornadofx.*

class MyApp : App(Workspace::class) {
    init {
        importStylesheet<AppStyles>()
        workspace.header.removeFromParent()
        workspace.add(MainMenu())
    }
    override fun onBeforeShow(view:UIComponent) {
        workspace.dock<ProjectHomeView>()
    }
}
//launch the org.wycliffeassociates.otter.jvm.app
fun main(args: Array<String>) {
    initApp()
    launch<MyApp>(args)
}

private fun initApp() {
    ImportLanguages(ClassLoader.getSystemResourceAsStream("langnames.json"), Injector.languageRepo)
            .import()
            .onErrorComplete()
            .subscribe()

    ImportAudioPlugins(Injector.audioPluginRegistrar, Injector.directoryProvider)
            .importAll()
            .andThen(InitializePlugins(Injector.pluginRepository).init())
            .subscribe()

    Injector.takeRepository
            .removeNonExistentTakes()
            .subscribe()
}
