package org.wycliffeassociates.otter.jvm.workbookapp

import javafx.stage.Stage
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.splash.view.SplashScreen
import tornadofx.*
import kotlin.reflect.KClass

class MyApp : App(SplashScreen::class) {
//    val persistenceComponent: PersistenceComponent
//    val audioComponent: AudioComponent
//    val audioPluginComponent: AudioPluginComponent
    val dependencyGraph = DaggerAppDependencyGraph.builder().build()

    init {
//        persistenceComponent = DaggerPersistenceComponent.builder().build()
//        audioComponent = DaggerAudioComponent.builder().build()
//        audioPluginComponent = DaggerAudioPluginComponent.builder().build()

        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T {
                return when (type) {
                    AppDatabase::class -> dependencyGraph.injectDatabase() as T
                    IDirectoryProvider::class -> dependencyGraph.injectDirectoryProvider() as T
                    IAudioPluginRegistrar::class -> dependencyGraph.injectRegistrar() as T
                    IAudioRecorder::class -> dependencyGraph.injectRecorder() as T
                    IAudioPlayer::class -> dependencyGraph.injectPlayer() as T
                    IAppPreferences::class -> dependencyGraph.injectPreferences() as T
                    IAudioPluginRepository::class -> dependencyGraph.injectAudioPluginRepository() as T
                    else -> null as T
                }
            }

        }

        initializeLogger(dependencyGraph.injectDirectoryProvider())
        importStylesheet<AppStyles>()
    }

    fun initializeLogger(directoryProvider: IDirectoryProvider) {
        ConfigureLogger(
            directoryProvider.logsDirectory
        ).configure()
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
