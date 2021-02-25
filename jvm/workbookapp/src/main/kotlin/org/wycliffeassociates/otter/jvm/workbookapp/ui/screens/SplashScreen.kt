package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SplashScreenViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.SplashScreenStyles
import tornadofx.*

class SplashScreen : View() {
    private val viewModel: SplashScreenViewModel by inject()
    override val root = stackpane {
        addStylesheet(SplashScreenStyles::class)
        addClass(SplashScreenStyles.splashRoot)
        alignment = Pos.TOP_CENTER
        imageview(Image(SplashScreen::class.java.getResourceAsStream("/orature_splash.png")))
        progressbar(viewModel.progressProperty) {
            addClass(SplashScreenStyles.splashProgress)
            prefWidth = 376.0
            translateY = 360.0
        }
    }

    init {
        viewModel.shouldCloseProperty.onChange {
            if (it) close()
        }
    }

    override fun onDock() {
        super.onDock()
        FX.primaryStage.scene.fill = Color.TRANSPARENT
    }
}
