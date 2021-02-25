package org.wycliffeassociates.otter.jvm.workbookapp

import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterExceptionHandler
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.SplashScreen
import tornadofx.*

class OtterApp : App(SplashScreen::class), IDependencyGraphProvider  {
    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()

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
        stage.initStyle(StageStyle.TRANSPARENT)
        super.start(stage)
    }
}

fun main(args: Array<String>) {
    launch<OtterApp>(args)
}
