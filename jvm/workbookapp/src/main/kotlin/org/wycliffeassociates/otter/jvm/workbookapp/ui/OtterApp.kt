package org.wycliffeassociates.otter.jvm.workbookapp.ui

import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.SnackbarHandler
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.RootView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.SplashScreen
import tornadofx.*
import tornadofx.FX.Companion.messages

class OtterApp : App(RootView::class), IDependencyGraphProvider {
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
    }

    override fun shouldShowPrimaryStage(): Boolean {
        return false
    }
}
