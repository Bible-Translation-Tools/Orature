package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.geometry.Pos
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.HomePage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SplashScreenViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.SplashScreenStyles
import tornadofx.*

class SplashScreen : View() {
    private val viewModel: SplashScreenViewModel by inject()
    private val navigator: NavigationMediator by inject()

    override val root = stackpane {
        addStylesheet(SplashScreenStyles::class)
        addClass(SplashScreenStyles.splashRoot)
        alignment = Pos.TOP_CENTER
        add(resources.imageview("/orature_splash.png"))
        progressbar(viewModel.progressProperty) {
            addClass(SplashScreenStyles.splashProgress)
            prefWidth = 376.0
            translateY = 360.0
        }
    }

    init {
        viewModel
            .initApp()
            .subscribe(
                {},
                { finish() },
                { finish() }
            )
    }

    private fun finish() {
        close()
        navigator.dock<HomePage>()
        primaryStage.show()
    }
}
