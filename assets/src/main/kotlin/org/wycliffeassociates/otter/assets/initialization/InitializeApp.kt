package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
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
    val resourceMetadataRepo: IResourceMetadataRepository,
    val resourceContainerRepo: IResourceContainerRepository,
    val collectionRepo: ICollectionRepository,
    val contentRepo: IContentRepository,
    val installedEntityRepo: IInstalledEntityRepository,
    val zipEntryTreeBuilder: IZipEntryTreeBuilder,
    val workbookRepository: IWorkbookRepository,
    val resourceRepository: IResourceRepository
) {

    private val logger = LoggerFactory.getLogger(InitializeApp::class.java)

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
                        resourceMetadataRepo,
                        resourceContainerRepo,
                        collectionRepo,
                        contentRepo,
                        takeRepo,
                        languageRepo,
                        directoryProvider,
                        zipEntryTreeBuilder,
                        resourceRepository
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
                    ),
                    InitializeProjects(
                        resourceMetadataRepo,
                        resourceContainerRepo,
                        collectionRepo,
                        contentRepo,
                        takeRepo,
                        languageRepo,
                        directoryProvider,
                        zipEntryTreeBuilder,
                        installedEntityRepo,
                        workbookRepository,
                        resourceRepository
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
            }
            .doOnError { e ->
                logger.error("Error in initApp", e)
            }
            .subscribeOn(Schedulers.io())
    }
}
