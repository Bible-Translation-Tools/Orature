package org.wycliffeassociates.otter.jvm.app.ui.splash.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.menu.view.MainMenu
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.view.ProjectHomeView
import tornadofx.*
import java.util.concurrent.TimeUnit

class SplashScreenViewModel : ViewModel() {
    val progressProperty = SimpleDoubleProperty(0.0)
    val shouldCloseProperty = SimpleBooleanProperty(false)
    private var newWorkspace: Workspace by singleAssign()

    init {
        initApp()
                .observeOnFx()
                .subscribe {
                    progressProperty.value = it
                    if (it == 1.0) {
                        newWorkspace = find()
                        newWorkspace.header.removeFromParent()
                        newWorkspace.add(MainMenu())
                        newWorkspace.dock<ProjectHomeView>()
                        newWorkspace.openWindow(owner = null)
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