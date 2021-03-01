package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import javafx.beans.property.SimpleDoubleProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.assets.initialization.InitializeApp
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import javax.inject.Inject

class SplashScreenViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(SplashScreenViewModel::class.java)

    @Inject
    lateinit var initApp: InitializeApp

    val progressProperty = SimpleDoubleProperty(0.0)

    fun initApp(): Observable<Double> {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        return initApp.initApp()
            .observeOnFx()
            .doOnError { logger.error("Error initializing app: ", it) }
            .map {
                progressProperty.value = it
                it
            }
    }
}
