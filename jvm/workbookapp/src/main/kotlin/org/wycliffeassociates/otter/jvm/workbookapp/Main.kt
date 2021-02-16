package org.wycliffeassociates.otter.jvm.workbookapp

import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.AppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterExceptionHandler
import org.wycliffeassociates.otter.jvm.workbookapp.ui.splash.view.SplashScreen
import tornadofx.*

interface DependencyGraphProvider {
    val dependencyGraph: AppDependencyGraph
}

class MyApp : App(SplashScreen::class), DependencyGraphProvider  {
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

// launch the org.wycliffeassociates.otter.jvm.workbookapp
fun main(args: Array<String>) {
    launch<MyApp>(args)
}
