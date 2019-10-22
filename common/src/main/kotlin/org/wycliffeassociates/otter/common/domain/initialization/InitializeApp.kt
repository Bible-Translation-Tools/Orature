package org.wycliffeassociates.otter.common.domain.initialization

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.data.config.Initialization
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.*
import java.io.File
import java.io.FileOutputStream

class InitializeApp(
    val preferences: IAppPreferences,
    val directoryProvider: IDirectoryProvider,
    val audioPluginRegistrar: IAudioPluginRegistrar,
    val pluginRepository: IAudioPluginRepository,
    val languageRepo: ILanguageRepository,
    val takeRepository: ITakeRepository,
    val resourceContainerRepo: IResourceContainerRepository,
    val initializationRepo: IInitializationRepository,
    val zipEntryTreeBuilder: IZipEntryTreeBuilder,
    val rcImporter: ImportResourceContainer = ImportResourceContainer(
        resourceContainerRepo,
        directoryProvider,
        zipEntryTreeBuilder
    )
) {
    fun initApp(): Observable<Double> {
        return Observable
            .fromPublisher<Double> {
                val initMap = initializationRepo.getAll().blockingGet().associateBy { it.name }

                it.onNext(0.0)

                var languagesInitialized = false
                initMap["langnames"]?.let {
                    languagesInitialized = it.initialized
                }
                if (!languagesInitialized) {
                    importLanguages().subscribe(
                        {
                            initMap["langnames"]?.let {
                                it.initialized = true
                                initializationRepo.update(it).blockingAwait()
                            } ?: initializationRepo.insert(
                                Initialization(
                                    "langnames",
                                    "0.0.1",
                                    true
                                )
                            ).ignoreElement().blockingAwait()
                        },
                        {
                            it.printStackTrace()
                        }
                    )
                }
                it.onNext(0.25)

                var recorderInitialized = false
                initMap["recorder"]?.let {
                    recorderInitialized = it.initialized
                }
                if (!recorderInitialized) {
                    importOtterRecorder().subscribe(
                        {
                            initMap["recorder"]?.let {
                                it.initialized = true
                                initializationRepo.update(it).blockingAwait()
                            } ?: initializationRepo.insert(
                                Initialization(
                                    "recorder",
                                    "0.0.1",
                                    true
                                )
                            ).ignoreElement().blockingAwait()
                        },
                        {
                            it.printStackTrace()
                        }
                    )
                }
                it.onNext(0.5)

                // Always import new plugins
                ImportAudioPlugins(audioPluginRegistrar, directoryProvider)
                    .importAll()
                    .andThen(pluginRepository.initSelected())
                    .blockingAwait()
                it.onNext(0.75)

                var ulbInitialized = false
                initMap["en_ulb"]?.let {
                    ulbInitialized = it.initialized
                }
                if (!ulbInitialized) {
                    rcImporter.import(
                        ClassLoader.getSystemResourceAsStream("content/en_ulb.zip")
                    ).subscribe(
                        {
                            initMap["en_ulb"]?.let {
                                it.initialized = true
                                initializationRepo.update(it).blockingAwait()
                            } ?: initializationRepo.insert(
                                Initialization(
                                    "en_ulb",
                                    "0.0.1",
                                    true
                                )
                            ).ignoreElement().blockingAwait()
                        },
                        {
                            it.printStackTrace()
                        }
                    )
                }

                // Always clean up database
                takeRepository
                    .removeNonExistentTakes()
                    .blockingAwait()
                it.onNext(1.0)
            }.subscribeOn(Schedulers.io())
    }

    private fun importLanguages(): Completable {
        return ImportLanguages(
            ClassLoader.getSystemResourceAsStream("content/langnames.json"),
            languageRepo
        ).import()
    }

    private fun importOtterRecorder(): Completable {
        val pluginsDir = directoryProvider.audioPluginDirectory
        val jar = File(pluginsDir, "OtterRecorder.jar")
        ClassLoader.getSystemResourceAsStream("plugins/jars/recorderapp.jar")
            ?.transferTo(FileOutputStream(jar))
        return pluginRepository.insert(
            AudioPluginData(
                0,
                "OtterRecorder",
                "1.0.0",
                false,
                true,
                jar.absolutePath,
                listOf(),
                null
            )
        ).doAfterSuccess { id: Int ->
            preferences.setRecorderPluginId(id)
        }.ignoreElement()
    }
}