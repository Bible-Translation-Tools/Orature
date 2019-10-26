package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.data.config.Initialization
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IInitializationRepository
import java.io.File
import java.io.FileOutputStream

class InitializeRecorder(
    val config: Initialization?,
    val directoryProvider: IDirectoryProvider,
    val pluginRepository: IAudioPluginRepository,
    val initializationRepo: IInitializationRepository,
    val preferences: IAppPreferences
) : Initializable {

    val log = LoggerFactory.getLogger(InitializeRecorder::class.java)

    override fun exec(): Completable {
        return if (config == null || !config.initialized) {
            log.info("Initializing recorder...")
            importOtterRecorder()
                .doOnComplete {
                    if (config != null) {
                        config.initialized = true
                        initializationRepo.update(config).blockingAwait()
                    } else {
                        initializationRepo.insert(
                            Initialization("recorder", "0.0.1", true)
                        ).ignoreElement().blockingAwait()
                    }
                }
                .doOnError { e ->
                    log.error("Error importing recorder.", e)
                }
                .doOnComplete {
                    log.info("Recorder imported!")
                }
                .subscribeOn(Schedulers.io())
        } else {
            log.info("Recorder up to date with version: ${config.version}")
            Completable.complete()
        }
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