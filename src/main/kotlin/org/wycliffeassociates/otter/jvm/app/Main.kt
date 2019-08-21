package org.wycliffeassociates.otter.jvm.app

import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.splash.view.SplashScreen
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

// launch the org.wycliffeassociates.otter.jvm.app
fun main(args: Array<String>) {
    launch<MyApp>(args)
}
