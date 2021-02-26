package org.wycliffeassociates.otter.jvm.workbookapp

import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterExceptionHandler
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view.MainMenu
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.MainScreenView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.SplashScreen
import tornadofx.*

class OtterApp : App(Workspace::class), IDependencyGraphProvider {
    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()
    var shouldBlockWindowCloseRequest = false

    init {
        val directoryProvider = dependencyGraph.injectDirectoryProvider()
        Thread.setDefaultUncaughtExceptionHandler(OtterExceptionHandler(directoryProvider))
        initializeLogger(directoryProvider)
        importStylesheet<AppStyles>()
    }

    fun initializeLogger(directoryProvider: IDirectoryProvider) {
        ConfigureLogger(
            directoryProvider.logsDirectory
        ).configure()
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.isMaximized = true
        stage.scene.window.setOnCloseRequest {
            if (shouldBlockWindowCloseRequest) {
                it.consume()
            }
        }
        find<SplashScreen>().openModal(StageStyle.UNDECORATED)
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.header.removeFromParent()
        workspace.add(MainMenu())
        workspace.dock<MainScreenView>()
    }

    override fun shouldShowPrimaryStage(): Boolean {
        return false
    }
}

fun main(args: Array<String>) {
    launch<OtterApp>(args)
}
