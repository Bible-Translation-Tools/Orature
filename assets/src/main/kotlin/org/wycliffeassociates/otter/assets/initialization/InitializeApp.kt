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
    val takeRepo: ITakeRepository,
    val resourceRepo: IResourceRepository,
    val resourceContainerRepo: IResourceContainerRepository,
    val collectionRepo: ICollectionRepository,
    val contentRepo: IContentRepository,
    val installedEntityRepo: IInstalledEntityRepository,
    val zipEntryTreeBuilder: IZipEntryTreeBuilder
) {

    fun initApp(): Observable<Double> {
        return Observable
            .fromPublisher<Double> { progress ->
                val initializers = listOf(
                    InitializeLanguages(
                        installedEntityRepo,
                        languageRepo
                    ),
                    InitializeUlb(
                        installedEntityRepo,
                        resourceRepo,
                        resourceContainerRepo,
                        collectionRepo,
                        contentRepo,
                        takeRepo,
                        languageRepo,
                        directoryProvider,
                        zipEntryTreeBuilder
                    ),
                    InitializeRecorder(
                        directoryProvider,
                        pluginRepository,
                        installedEntityRepo,
                        preferences
                    ),
                    InitializePlugins(
                        directoryProvider,
                        audioPluginRegistrar,
                        pluginRepository
                    ),
                    InitializeTakeRepository(
                        takeRepo
                    )
                )

                var total = 0.0
                val increment = (1.0).div(initializers.size)
                initializers.forEach {
                    total += increment
                    progress.onNext(total)
                    it.exec().blockingAwait()
                }
                progress.onComplete()
            }.subscribeOn(Schedulers.io())
    }
}