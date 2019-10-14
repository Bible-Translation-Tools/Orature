package org.wycliffeassociates.otter.common.domain.initialization

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
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
                it.onNext(0.0)
                it.onNext(0.25)

                val initialized = preferences.appInitialized().blockingGet()
                if (!initialized) {
                    importLanguages()
                    importOtterRecorder()
                    preferences.setAppInitialized(true).blockingAwait()
                }
                it.onNext(0.5)

                // Always import new plugins
                ImportAudioPlugins(audioPluginRegistrar, directoryProvider)
                    .importAll()
                    .andThen(pluginRepository.initSelected())
                    .blockingAwait()
                it.onNext(0.75)

                if (!initialized) {
                    rcImporter.import(ClassLoader.getSystemResourceAsStream("content/en_ulb.zip")).blockingGet()
                }

                // Always clean up database
                takeRepository
                    .removeNonExistentTakes()
                    .blockingAwait()
                it.onNext(1.0)
            }.subscribeOn(Schedulers.io())
    }

    private fun importLanguages() {
        ImportLanguages(
            ClassLoader.getSystemResourceAsStream("content/langnames.json"),
            languageRepo
        )
            .import()
            .onErrorComplete()
            .blockingAwait()
    }

    private fun importOtterRecorder() {
        val pluginsDir = directoryProvider.audioPluginDirectory
        val jar = File(pluginsDir, "OtterRecorder.jar")
        ClassLoader.getSystemResourceAsStream("plugins/jars/recorderapp.jar")
            ?.transferTo(FileOutputStream(jar))
        pluginRepository.insert(
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
        ).subscribe { id: Int ->
            preferences.setRecorderPluginId(id).blockingAwait()
        }
    }
}