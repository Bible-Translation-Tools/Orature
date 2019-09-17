package org.wycliffeassociates.otter.jvm.app.ui.splash.view

import javafx.event.EventHandler
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.ui.splash.viewmodel.SplashScreenViewModel
import tornadofx.*

class SplashScreen : View() {
    private val viewModel: SplashScreenViewModel by inject()
    private var offset = Pair(0.0, 0.0)
    override val root = vbox {
        addClass(SplashScreenStyles.splashRoot)
        progressbar(viewModel.progressProperty) {
            addClass(SplashScreenStyles.splashProgress)
        }
        onMousePressed = EventHandler {
            offset = Pair(it.sceneX, it.sceneY)
        }
        onMouseDragged = EventHandler {
            primaryStage.x = it.screenX - offset.first
            primaryStage.y = it.screenY - offset.second
        }
    }

    init {
        importStylesheet<SplashScreenStyles>()
        viewModel.shouldCloseProperty.onChange {
            if (it) close()
        }
    }

    override fun onDock() {
        super.onDock()
        FX.primaryStage.scene.fill = Color.TRANSPARENT
    }
}