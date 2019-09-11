package org.wycliffeassociates.otter.jvm.app.ui.splash.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.jvm.app.ui.AppWorkspace
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.app.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.app.ui.menu.view.MainMenu
import tornadofx.*

class SplashScreenViewModel : ViewModel() {
    val progressProperty = SimpleDoubleProperty(0.0)
    val shouldCloseProperty = SimpleBooleanProperty(false)
    private var appWorkspace: AppWorkspace by singleAssign()
    private val chromeableStage: ChromeableStage by inject()

    init {
        initApp()
                .observeOnFx()
                .subscribe {
                    progressProperty.value = it
                    if (it == 1.0) {
                        appWorkspace = find()
                        appWorkspace.header.removeFromParent()
                        appWorkspace.add(MainMenu())
                        appWorkspace.dock<MainScreenView>()
                        appWorkspace.openWindow(owner = null)
                        chromeableStage.navigateTo(TabGroupType.PROJECT)
                        shouldCloseProperty.value = true
                    }
                }
    }

    private fun initApp(): Observable<Double> {
        return Observable
                .fromPublisher<Double> {
                    it.onNext(0.0)
                    val injector: Injector = find()
                    it.onNext(0.25)

                    val initialized = injector.preferences.appInitialized().blockingGet()
                    if (!initialized) {
                        // Needs initialization
                        ImportLanguages(ClassLoader.getSystemResourceAsStream("content/langnames.json"), injector.languageRepo)
                                .import()
                                .onErrorComplete()
                                .blockingAwait()

                        injector.preferences.setAppInitialized(true).blockingAwait()
                    }
                    it.onNext(0.5)

                    // Always import new plugins
                    ImportAudioPlugins(injector.audioPluginRegistrar, injector.directoryProvider)
                            .importAll()
                            .andThen(injector.pluginRepository.initSelected())
                            .blockingAwait()
                    it.onNext(0.75)

                    // Always clean up database
                    injector.takeRepository
                            .removeNonExistentTakes()
                            .blockingAwait()
                    it.onNext(1.0)
                }.subscribeOn(Schedulers.io())
    }
}