package org.wycliffeassociates.otter.jvm.workbookapp.ui.splash.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.assets.initialization.InitializeApp
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.workbookapp.DependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.MyApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view.MainMenu
import tornadofx.*
import javax.inject.Inject

class SplashScreenViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(SplashScreenViewModel::class.java)

    @Inject
    lateinit var initApp: InitializeApp

    val progressProperty = SimpleDoubleProperty(0.0)
    val shouldCloseProperty = SimpleBooleanProperty(false)
    private val chromeableStage: ChromeableStage by inject()

    init {
        (app as DependencyGraphProvider).dependencyGraph.inject(this)

        initApp.initApp()
            .observeOnFx()
            .doOnComplete {
                openApplicationWindow()
            }
            .subscribe(
                {
                    progressProperty.value = it
                },
                {
                    logger.error("Error initializing app: ", it)
                    openApplicationWindow()
                }
            )
    }

    private fun openApplicationWindow() {
        workspace.header.removeFromParent()
        workspace.add(MainMenu())
        workspace.dock<MainScreenView>()
        workspace.openWindow(owner = null)
        chromeableStage.navigateTo(TabGroupType.PROJECT)
        shouldCloseProperty.value = true
    }
}
