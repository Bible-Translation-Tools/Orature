package org.wycliffeassociates.otter.jvm.workbookapp

import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.DaggerPersistenceComponent
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterExceptionHandler
import org.wycliffeassociates.otter.jvm.workbookapp.ui.splash.view.SplashScreen
import tornadofx.*
import java.lang.RuntimeException

class MyApp : App(SplashScreen::class) {
    init {
        importStylesheet<AppStyles>()
        Thread.setDefaultUncaughtExceptionHandler(OtterExceptionHandler())
    }

    override fun start(stage: Stage) {
        stage.initStyle(StageStyle.TRANSPARENT)
        super.start(stage)
    }
}

// launch the org.wycliffeassociates.otter.jvm.workbookapp
fun main(args: Array<String>) {
    initializeLogger()
    launch<MyApp>(args)
}

fun initializeLogger() {
    val persistenceComponent = DaggerPersistenceComponent.builder().build()
    val directoryProvider = persistenceComponent.injectDirectoryProvider()
    ConfigureLogger(directoryProvider.logsDirectory).configure()
}