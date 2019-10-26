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
                val initMap = initializationRepo.getAll().blockingGet().associateBy { it.name }
                val initializers = listOf(
                    InitializeLanguages(
                        initMap["langnames"],
                        initializationRepo,
                        languageRepo
                    ),
                    InitializeUlb(
                        initMap["en_ulb"],
                        initializationRepo,
                        resourceContainerRepo,
                        directoryProvider,
                        zipEntryTreeBuilder
                    ),
                    InitializeRecorder(
                        initMap["recorder"],
                        directoryProvider,
                        pluginRepository,
                        initializationRepo,
                        preferences
                    ),
                    InitializePlugins(
                        initMap["ocenaudio"],
                        directoryProvider,
                        audioPluginRegistrar,
                        pluginRepository,
                        initializationRepo
                    ),
                    InitializeTakeRepository(
                        takeRepository
                    )
                )

                var total = 0.0
                val increment = (1.0).div(initializers.size)
                initializers.forEach { init ->
                    init.exec().blockingAwait()
                    total += increment
                    progress.onNext(total)
                }
                progress.onComplete()
            }.subscribeOn(Schedulers.io())
    }
}