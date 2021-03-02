package org.wycliffeassociates.otter.jvm.workbookapp

import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginClosedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.PluginOpenedEvent
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterExceptionHandler
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view.MainMenu
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.MainScreenView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.SplashScreen
import tornadofx.*
import tornadofx.FX.Companion.messages

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
                SnackbarHandler.enqueue(messages["applicationCloseBlocked"])
            }
        }
        find<SplashScreen>().openModal(StageStyle.UNDECORATED)
    }

    override fun onBeforeShow(view: UIComponent) {
        // Configure Snackbar Handler to display received snackbars on the root window
        SnackbarHandler.setWindowRoot(view.root as Pane)

        // Configure the Workspace: sets up the window menu and external app open events
        val menu = MainMenu()
        // Plugins being opened should block the app from closing as this could result in a
        // loss of communication between the app and the external plugin, thus data loss
        workspace.subscribe<PluginOpenedEvent> {
            shouldBlockWindowCloseRequest = true
            menu.managedProperty().set(false)
        }
        workspace.subscribe<PluginClosedEvent> {
            shouldBlockWindowCloseRequest = false
            menu.managedProperty().set(true)
        }
        workspace.add(menu)
        workspace.header.removeFromParent()
        workspace.dock<MainScreenView>()
    }

    override fun shouldShowPrimaryStage(): Boolean {
        return false
    }
}

fun main(args: Array<String>) {
    launch<OtterApp>(args)
}
