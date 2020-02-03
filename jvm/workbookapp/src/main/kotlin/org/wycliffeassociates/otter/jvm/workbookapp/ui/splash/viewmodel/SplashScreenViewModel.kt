package org.wycliffeassociates.otter.jvm.workbookapp.ui.splash.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.assets.initialization.InitializeApp
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view.MainMenu
import tornadofx.*

class SplashScreenViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(SplashScreenViewModel::class.java)

    private val injector: Injector = find()
    private val initApp = InitializeApp(
        injector.preferences,
        injector.directoryProvider,
        injector.audioPluginRegistrar,
        injector.pluginRepository,
        injector.languageRepo,
        injector.takeRepository,
        injector.resourceRepository,
        injector.resourceContainerRepository,
        injector.collectionRepo,
        injector.contentRepository,
        injector.installedEntityRepository,
        injector.zipEntryTreeBuilder
    )
    val progressProperty = SimpleDoubleProperty(0.0)
    val shouldCloseProperty = SimpleBooleanProperty(false)
    private val chromeableStage: ChromeableStage by inject()

    init {
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