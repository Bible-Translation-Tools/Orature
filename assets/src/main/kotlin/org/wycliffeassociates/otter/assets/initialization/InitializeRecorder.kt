package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import java.io.File
import java.io.FileOutputStream

class InitializeRecorder(
    val directoryProvider: IDirectoryProvider,
    val pluginRepository: IAudioPluginRepository,
    val installedEntityRepo: IInstalledEntityRepository,
    val preferences: IAppPreferences
) : Installable {

    override val name = "RECORDER"
    override val version = 1

    val log = LoggerFactory.getLogger(InitializeRecorder::class.java)

    override fun exec(): Completable {
        return Completable
            .fromCallable {
                var installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    log.info("Initializing $name version: $version...")
                    importOtterRecorder()
                        .doOnComplete {
                            installedEntityRepo.install(this)
                            log.info("Recorder imported!")
                            log.info("$name version: $version installed!")
                        }
                        .doOnError { e ->
                            log.error("Error importing recorder.", e)
                        }
                        .blockingAwait()
                } else {
                    log.info("$name up to date with version: $version")
                }
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
                canEdit = false,
                canRecord = true,
                executable = jar.absolutePath,
                args = listOf(),
                pluginFile = null
            )
        ).doAfterSuccess { id: Int ->
            preferences.setRecorderPluginId(id)
        }.ignoreElement()
    }
}