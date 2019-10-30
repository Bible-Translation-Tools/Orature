package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.*

class InitializeApp(
    val preferences: IAppPreferences,
    val directoryProvider: IDirectoryProvider,
    val audioPluginRegistrar: IAudioPluginRegistrar,
    val pluginRepository: IAudioPluginRepository,
    val languageRepo: ILanguageRepository,
    val takeRepository: ITakeRepository,
    val resourceContainerRepo: IResourceContainerRepository,
    val initializationRepo: IInitializationRepository,
    val zipEntryTreeBuilder: IZipEntryTreeBuilder
) {

    fun initApp(): Observable<Double> {
        return Observable
            .fromPublisher<Double> { progress ->
                val initializers = listOf(
                    InitializeLanguages(
                        initializationRepo,
                        languageRepo
                    ),
                    InitializeUlb(
                        initializationRepo,
                        resourceContainerRepo,
                        directoryProvider,
                        zipEntryTreeBuilder
                    ),
                    InitializeRecorder(
                        directoryProvider,
                        pluginRepository,
                        initializationRepo,
                        preferences
                    ),
                    InitializePlugins(
                        directoryProvider,
                        audioPluginRegistrar,
                        pluginRepository
                    ),
                    InitializeTakeRepository(
                        takeRepository
                    )
                ).map {
                    it.exec()
                }

                var total = 0.0
                val increment = (1.0).div(initializers.size)
                initializers.reduceRight { init, next ->
                    total += increment
                    progress.onNext(total)
                    init.andThen(next)
                }.doFinally {
                    progress.onComplete()
                }.subscribe()
            }.subscribeOn(Schedulers.io())
    }
}