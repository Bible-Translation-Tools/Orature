package org.wycliffeassociates.otter.jvm.workbookapp

import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.splash.view.SplashScreen
import tornadofx.*

class MyApp : App(SplashScreen::class) {
    init {
        importStylesheet<AppStyles>()
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
